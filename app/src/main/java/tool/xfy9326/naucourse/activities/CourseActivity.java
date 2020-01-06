package tool.xfy9326.naucourse.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.CourseListAsync;
import tool.xfy9326.naucourse.asyncTasks.CourseNextListAsync;
import tool.xfy9326.naucourse.beans.SchoolTime;
import tool.xfy9326.naucourse.beans.course.Course;
import tool.xfy9326.naucourse.handlers.MainHandler;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.PermissionMethod;
import tool.xfy9326.naucourse.methods.async.SchoolTimeMethod;
import tool.xfy9326.naucourse.methods.async.TableMethod;
import tool.xfy9326.naucourse.methods.compute.CourseEditMethod;
import tool.xfy9326.naucourse.methods.compute.TimeMethod;
import tool.xfy9326.naucourse.methods.io.BackupMethod;
import tool.xfy9326.naucourse.methods.io.DataMethod;
import tool.xfy9326.naucourse.methods.io.ShareMethod;
import tool.xfy9326.naucourse.methods.view.DialogMethod;
import tool.xfy9326.naucourse.views.recyclerAdapters.CourseAdapter;

public class CourseActivity extends AppCompatActivity {
    public static final int COURSE_EDIT_REQUEST_CODE = 1;
    private static final int COURSE_ADD_REQUEST_CODE = 2;
    private static final int BACKUP_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE = 3;
    private static final int RECOVER_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE = 4;
    private static final int CHOOSE_RECOVER_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE = 5;
    private static final int CHOOSE_RECOVER_FILE_REQUEST_CODE = 6;
    private final ArrayList<Course> courseArrayList = new ArrayList<>();
    public boolean activityDestroy = true;
    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private int lastOffset = 0;
    private int lastPosition = 0;
    private boolean needSave = false;
    private boolean needReload = false;
    private Dialog loadingDialog = null;

    private String customStartTermDate = null;
    private String customEndTermDate = null;
    private Dialog termSetDialog = null;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        activityDestroy = false;
        BaseMethod.getApp(this).setCourseActivity(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        toolBarSet();
        viewSet();
        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //保存数据
            case R.id.menu_course_save:
                saveData();
                needReload = true;
                break;
            //返回
            case android.R.id.home:
                saveCheck();
                return true;

            case R.id.menu_course_random_course_color:
                randomSetCourseColor();
                break;
            case R.id.menu_course_share_import:
                importSharedCourse();
                break;
            //清空课程
            case R.id.menu_course_delete_all:
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.confirm_delete_all, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, v -> {
                    courseArrayList.clear();
                    courseAdapter.clearAll();
                    needSave = true;
                    FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton_course_add);
                    if (floatingActionButton.getVisibility() != View.VISIBLE) {
                        floatingActionButton.setVisibility(View.VISIBLE);
                        ViewCompat.animate(floatingActionButton).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                                .setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener(null)
                                .start();
                    }
                }).show();
                break;
            //学期设定
            case R.id.menu_course_term_date:
                setTermDate();
                break;
            case R.id.menu_course_backup:
                if (PermissionMethod.checkStoragePermission(this, BACKUP_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE)) {
                    backupCourse();
                }
                break;
            case R.id.menu_course_recover:
                if (PermissionMethod.checkStoragePermission(this, RECOVER_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE)) {
                    recoverCourse();
                }
                break;
            case R.id.menu_course_choose_recover:
                if (PermissionMethod.checkStoragePermission(this, CHOOSE_RECOVER_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE)) {
                    chooseRecoverCourse();
                }
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    synchronized private void getData() {
        loadingDialog = DialogMethod.showLoadingDialog(CourseActivity.this, true, dialog -> finish());
        new Thread(() -> {
            courseArrayList.clear();
            ArrayList<Course> temp = DataMethod.getOfflineTableData(CourseActivity.this);
            if (temp != null && !temp.isEmpty()) {
                courseArrayList.addAll(temp);
            }
            runOnUiThread(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if (!activityDestroy) {
                    if (courseAdapter == null) {
                        courseAdapter = new CourseAdapter(CourseActivity.this, courseArrayList);
                        recyclerView.setAdapter(courseAdapter);
                    } else {
                        courseAdapter.updateList();
                    }
                }
            });
        }).start();
    }

    private void viewSet() {
        recyclerView = findViewById(R.id.recyclerView_course_list);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //保证从其他视图返回时列表位置不变
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });
        if (courseAdapter == null) {
            courseAdapter = new CourseAdapter(CourseActivity.this, courseArrayList);
        }
        recyclerView.setAdapter(courseAdapter);
        scrollToPosition();

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton_course_add);
        floatingActionButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);
            builder.setItems(new String[]{getString(R.string.create_new_course), getString(R.string.import_course_from_jwc_current), getString(R.string.import_course_from_jwc_next)}, (dialog, which) -> {
                switch (which) {
                    case 0:
                        createNewCourse();
                        break;
                    case 1:
                        importDataFromJwc();
                        break;
                    case 2:
                        importDataFromJwcNext();
                        break;
                    default:
                }
            });
            builder.show();
        });

        autoUpdateCourseAlert();
    }

    private void importSharedCourse() {
        String shareStr = ShareMethod.getStringFromClipBoard(this);
        if (shareStr != null && !shareStr.isEmpty() && shareStr.startsWith(Config.SHARE_COURSE_PREFIX)) {
            Course course = ShareMethod.getShareCourse(shareStr);
            if (course != null && courseAdapter != null) {
                courseArrayList.add(course);
                courseAdapter.updateList();
                needSave = true;
            } else {
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.import_share_course_error, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.import_share_course_empty, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void createNewCourse() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.dialog_course_term_set, findViewById(R.id.layout_dialog_course_term_set));

        AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);
        builder.setTitle(R.string.course_term_set);
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            EditText editTextYear = view.findViewById(R.id.editText_course_school_year);
            RadioButton radioButtonTermOne = view.findViewById(R.id.radioButton_term_one);
            String strYear = editTextYear.getText().toString();
            if (!strYear.isEmpty() && BaseMethod.isInteger(strYear)) {
                long year = Long.valueOf(strYear);
                if (year >= 1983L && year < 10000L) {
                    //仅支持四位数的年份，仅支持一年两学期制
                    long term = (year * 10000L + year + 1L) * 10L + (radioButtonTermOne.isChecked() ? 1L : 2L);
                    Intent intent = new Intent(CourseActivity.this, CourseEditActivity.class);
                    intent.putExtra(Config.INTENT_ADD_COURSE, true);
                    intent.putExtra(Config.INTENT_ADD_COURSE_TERM, term);
                    startActivityForResult(intent, COURSE_ADD_REQUEST_CODE);
                } else {
                    Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.input_error, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.input_error, Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNeutralButton(R.string.use_now_term, (dialog, which) -> {
            String term = TimeMethod.getNowShowTerm(CourseActivity.this);
            if (term != null) {
                long termLong = Long.valueOf(term);
                Intent intent = new Intent(CourseActivity.this, CourseEditActivity.class);
                intent.putExtra(Config.INTENT_ADD_COURSE, true);
                intent.putExtra(Config.INTENT_ADD_COURSE_TERM, termLong);
                startActivityForResult(intent, COURSE_ADD_REQUEST_CODE);
            } else {
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.input_error, Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.show();
    }

    private void autoUpdateCourseAlert() {
        if (sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE)) {
            if (!sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.attention);
                builder.setMessage(R.string.auto_update_course_table_alert);
                builder.setPositiveButton(android.R.string.yes, null);
                builder.setNeutralButton(R.string.no_alert_again, (dialog, which) -> sharedPreferences.edit().putBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT, true).apply());
                builder.show();
            }
        }
    }

    //还原下拉的列表位置
    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) Objects.requireNonNull(recyclerView).getLayoutManager();
        View topView = null;
        if (layoutManager != null) {
            topView = layoutManager.getChildAt(0);
        }
        if (topView != null) {
            lastOffset = topView.getTop();
            lastPosition = layoutManager.getPosition(topView);
        }
    }

    private void scrollToPosition() {
        if (recyclerView != null) {
            if (recyclerView.getLayoutManager() != null && lastPosition >= 0) {
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
            }
        }
    }

    @Override
    public void onBackPressed() {
        saveCheck();
    }

    //保存检查
    private void saveCheck() {
        if (needSave) {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.save_attention, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, v -> finish()).show();
        } else {
            finish();
        }
    }

    //外部设置数据已经变动
    public void setArrayChanged() {
        needSave = true;
    }

    //保存数据
    synchronized private void saveData() {
        loadingDialog = DialogMethod.showLoadingDialog(CourseActivity.this, false, null);
        new Thread(() -> {
            final CourseEditMethod.CourseCheckResult checkResult = CourseEditMethod.checkCourseList(courseArrayList);
            if (checkResult.isHasError()) {
                final String checkName = checkResult.getCheckCourseName();
                final String conflictName = checkResult.getConflictCourseName();
                runOnUiThread(() -> {
                    if (checkName != null && conflictName != null) {
                        Snackbar.make(findViewById(R.id.layout_course_manage_content), getString(R.string.course_edit_error_conflict, checkName, conflictName), Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.course_edit_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (DataMethod.saveOfflineData(CourseActivity.this, courseArrayList, TableMethod.FILE_NAME, false, TableMethod.IS_ENCRYPT)) {
                    needSave = false;
                    runOnUiThread(() -> Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.save_success, Snackbar.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.save_failed, Snackbar.LENGTH_SHORT).show());
                }
            }
            runOnUiThread(() -> {
                if (!activityDestroy) {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.cancel();
                        loadingDialog = null;
                    }
                }
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == COURSE_ADD_REQUEST_CODE || requestCode == COURSE_EDIT_REQUEST_CODE) {
                if (data.hasExtra(Config.INTENT_EDIT_COURSE_ITEM)) {
                    Course course = (Course) data.getSerializableExtra(Config.INTENT_EDIT_COURSE_ITEM);
                    if (course != null) {
                        boolean found = false;
                        int i;
                        for (i = 0; courseAdapter != null && i < courseArrayList.size() && !found; i++) {
                            if (Objects.requireNonNull(courseArrayList.get(i).getCourseId()).equalsIgnoreCase(course.getCourseId())) {
                                courseArrayList.set(i, course);
                                found = true;
                            }
                        }
                        if (!found) {
                            courseArrayList.add(course);
                        }
                        needSave = true;
                        if (courseAdapter == null) {
                            courseAdapter = new CourseAdapter(CourseActivity.this, courseArrayList);
                            recyclerView.setAdapter(courseAdapter);
                        } else {
                            courseAdapter.updateList();
                        }
                        if (!found) {
                            courseAdapter.notifyItemRangeInserted(courseArrayList.size() - 1, 1);
                        } else {
                            courseAdapter.notifyItemChanged(i - 1);
                        }
                    }
                }
            } else if (requestCode == CHOOSE_RECOVER_FILE_REQUEST_CODE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        ArrayList<Course> courses = BackupMethod.restoreCourse(CourseActivity.this, uri);
                        if (courses != null) {
                            addCourseList(courses, false, true, false, false);
                            super.onActivityResult(requestCode, resultCode, data);
                            return;
                        }
                    }
                }
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.recover_failed, Snackbar.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //从教务处导入本学期课程数据
    private void importDataFromJwc() {
        CourseListAsync courseListAsync = new CourseListAsync();
        loadingDialog = DialogMethod.showLoadingDialog(CourseActivity.this, true, dialog -> {
            courseListAsync.cancel(true);
            closeLoadingDialog();
        });
        courseListAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }


    private void importDataFromJwcNext() {
        if (sharedPreferences.getBoolean(Config.PREFERENCE_NEED_CUSTOM_TERM_ALERT, Config.DEFAULT_PREFERENCE_NEED_CUSTOM_TERM_ALERT)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);
            builder.setTitle(R.string.attention);
            builder.setMessage(R.string.import_next_term_alert);
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> importNextDataTermFromJwc());
            builder.setNeutralButton(R.string.confirm_and_not_show_again, (dialog, which) -> {
                sharedPreferences.edit().putBoolean(Config.PREFERENCE_NEED_CUSTOM_TERM_ALERT, false).apply();
                importNextDataTermFromJwc();
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        } else {
            importNextDataTermFromJwc();
        }
    }

    //从教务处导入下学期课程数据
    private void importNextDataTermFromJwc() {
        CourseNextListAsync courseNextListAsync = new CourseNextListAsync();
        loadingDialog = DialogMethod.showLoadingDialog(CourseActivity.this, true, dialog -> {
            courseNextListAsync.cancel(true);
            closeLoadingDialog();
        });
        courseNextListAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }

    /**
     * 接收导入的数据并显示
     *
     * @param courses                   课程数据列表
     * @param isCurrentTerm             是否是当前学期的课程表
     * @param isBackup                  是否是备份导入
     * @param nextTermCourseImportError 下学期课表是否导入失败
     * @param combineColor              是否合并颜色
     */
    public void addCourseList(final ArrayList<Course> courses, boolean isCurrentTerm, final boolean isBackup, boolean nextTermCourseImportError, final boolean combineColor) {
        if (!activityDestroy) {
            closeLoadingDialog();
            if (nextTermCourseImportError) {
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.next_term_course_error, Snackbar.LENGTH_SHORT).show();
            } else {
                if (courses != null && courses.size() != 0) {
                    String[] name = new String[courses.size()];
                    final boolean[] checked = new boolean[courses.size()];
                    for (int i = 0; i < courses.size(); i++) {
                        name[i] = courses.get(i).getCourseName();
                        checked[i] = true;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(isBackup ? R.string.backup_course : isCurrentTerm ? R.string.import_course_from_jwc_current : R.string.import_course_from_jwc_next);
                    builder.setMultiChoiceItems(name, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
                    builder.setPositiveButton(R.string.add_course_and_clean, (dialog, which) -> chooseCourseAdd(checked, courses, true, isBackup, combineColor));
                    builder.setNeutralButton(R.string.add_course_only, (dialog, which) -> chooseCourseAdd(checked, courses, false, isBackup, combineColor));
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> courses.clear());
                    builder.show();
                } else {
                    Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.course_empty, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void closeLoadingDialog() {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing()) {
                loadingDialog.cancel();
            }
        }
    }

    private void chooseCourseAdd(boolean[] checked, ArrayList<Course> courses, boolean termCheck, boolean isBackup, boolean combineColor) {
        ArrayList<Course> coursesChoose = new ArrayList<>();
        for (int i = 0; i < checked.length; i++) {
            if (checked[i]) {
                coursesChoose.add(courses.get(i));
            }
        }
        ArrayList<Course> list = CourseEditMethod.combineCourseList(coursesChoose, courseArrayList, termCheck, false, combineColor);
        if (list != null) {
            courseArrayList.clear();
            courseArrayList.addAll(list);

            courseAdapter.updateList();
            needSave = true;
        }
        if (isBackup) {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.recover_success, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.add_success, Snackbar.LENGTH_SHORT).show();
        }

        boolean needTermSet = true;
        String nowTerm = TimeMethod.getNowShowTerm(this);
        for (Course course : courseArrayList) {
            if (course.getCourseTerm().equals(nowTerm)) {
                needTermSet = false;
                break;
            }
        }
        if (needTermSet) {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.need_set_course_term, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .setAction(R.string.course_term_set, v -> setTermDate())
                    .show();
        }
    }

    private void setTermDate() {
        customStartTermDate = sharedPreferences.getString(Config.PREFERENCE_CUSTOM_TERM_START_DATE, null);
        customEndTermDate = sharedPreferences.getString(Config.PREFERENCE_CUSTOM_TERM_END_DATE, null);
        showTermSetDialog();
    }

    private void showTermSetDialog() {
        final Calendar calendarStart = Calendar.getInstance(Locale.CHINA);
        final Calendar calendarEnd = Calendar.getInstance(Locale.CHINA);

        final SchoolTime schoolTime = (SchoolTime) DataMethod.getOfflineData(this, SchoolTime.class, SchoolTimeMethod.FILE_NAME, SchoolTimeMethod.IS_ENCRYPT);
        if (schoolTime != null) {
            try {
                calendarStart.setTime(TimeMethod.parseDateSDF(schoolTime.getStartTime()));
                calendarEnd.setTime(TimeMethod.parseDateSDF(schoolTime.getEndTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (customStartTermDate != null && customEndTermDate != null) {
            try {
                calendarStart.setTime(TimeMethod.parseDateSDF(customStartTermDate));
                calendarEnd.setTime(TimeMethod.parseDateSDF(customEndTermDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            customStartTermDate = TimeMethod.formatDateSDF(calendarStart.getTime());
            customEndTermDate = TimeMethod.formatDateSDF(calendarEnd.getTime());
        }

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_term_time_set, findViewById(R.id.layout_dialog_term_time_set));

        TextView textViewStart = view.findViewById(R.id.textView_custom_start_date);
        textViewStart.setText(getString(R.string.custom_term_start_date, customStartTermDate));

        TextView textViewEnd = view.findViewById(R.id.textView_custom_end_date);
        textViewEnd.setText(getString(R.string.custom_term_end_date, customEndTermDate));

        Button buttonStart = view.findViewById(R.id.button_custom_start_date);
        buttonStart.setOnClickListener(v -> {
            if (termSetDialog != null) {
                if (termSetDialog.isShowing()) {
                    termSetDialog.cancel();
                }
            }
            showDatePickDialog(true, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH));
        });

        Button buttonEnd = view.findViewById(R.id.button_custom_end_date);
        buttonEnd.setOnClickListener(v -> {
            if (termSetDialog != null) {
                if (termSetDialog.isShowing()) {
                    termSetDialog.cancel();
                }
            }
            showDatePickDialog(false, calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH), calendarEnd.get(Calendar.DAY_OF_MONTH));
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.custom_term);
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            if (customStartTermDate != null && customEndTermDate != null) {
                try {
                    long termDay = TimeMethod.parseDateSDF(customEndTermDate).getTime() - TimeMethod.parseDateSDF(customStartTermDate).getTime();
                    if (termDay <= 0 || termDay / (1000 * 60 * 60 * 24) < 30 || termDay / (1000 * 60 * 60 * 24) > (7 * Config.DEFAULT_MAX_WEEK)) {
                        Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.term_set_failed, Snackbar.LENGTH_SHORT).show();
                    } else {
                        needReload = true;
                        if (schoolTime != null) {
                            sharedPreferences.edit().putString(Config.PREFERENCE_OLD_TERM_START_DATE, schoolTime.getStartTime())
                                    .putString(Config.PREFERENCE_OLD_TERM_END_DATE, schoolTime.getEndTime()).apply();
                        }
                        sharedPreferences.edit().putString(Config.PREFERENCE_CUSTOM_TERM_START_DATE, customStartTermDate)
                                .putString(Config.PREFERENCE_CUSTOM_TERM_END_DATE, customEndTermDate).apply();
                        Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.term_set_success, Snackbar.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.reset, (dialog, which) -> {
            sharedPreferences.edit().remove(Config.PREFERENCE_OLD_TERM_START_DATE)
                    .remove(Config.PREFERENCE_OLD_TERM_END_DATE)
                    .remove(Config.PREFERENCE_CUSTOM_TERM_START_DATE)
                    .remove(Config.PREFERENCE_CUSTOM_TERM_END_DATE).apply();
            needReload = true;
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.term_set_success, Snackbar.LENGTH_SHORT).show();
        });
        builder.setView(view);
        termSetDialog = builder.show();
    }

    private void showDatePickDialog(final boolean isStartDate, int year, int month, int day) {
        final DatePickerDialog pickerDialog = new DatePickerDialog(this, null, year, month, day);
        pickerDialog.setCancelable(false);
        pickerDialog.setCanceledOnTouchOutside(false);
        pickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), (dialog, which) -> {
            DatePicker datePicker = pickerDialog.getDatePicker();
            DecimalFormat df = new DecimalFormat("00");
            String getDate = datePicker.getYear() + "-" + df.format(datePicker.getMonth() + 1) + "-" + df.format(datePicker.getDayOfMonth());
            if (isStartDate) {
                customStartTermDate = getDate;
            } else {
                customEndTermDate = getDate;
            }
            showTermSetDialog();
        });
        pickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), (dialog, which) -> showTermSetDialog());
        pickerDialog.show();
    }

    private void backupCourse() {
        if (BackupMethod.backupCourse(CourseActivity.this, courseArrayList)) {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.backup_success, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.backup_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void chooseRecoverCourse() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, CHOOSE_RECOVER_FILE_REQUEST_CODE);
    }

    private void recoverCourse() {
        ArrayList<Course> courses = BackupMethod.restoreCourse(CourseActivity.this);
        if (courses == null) {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.recover_failed, Snackbar.LENGTH_SHORT).show();
        } else {
            addCourseList(courses, false, true, false, false);
        }
    }

    private void randomSetCourseColor() {
        needSave = true;
        for (Course course : courseArrayList) {
            course.setCourseColor(BaseMethod.getRandomColor(CourseActivity.this));
        }
        courseAdapter.updateList();
        Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.random_course_color_success, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BACKUP_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE || requestCode == RECOVER_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE || requestCode == CHOOSE_RECOVER_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            boolean requestSuccess = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    requestSuccess = false;
                    break;
                }
            }
            if (requestSuccess) {
                if (requestCode == BACKUP_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE) {
                    backupCourse();
                } else if (requestCode == RECOVER_WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE) {
                    recoverCourse();
                } else {
                    chooseRecoverCourse();
                }
            } else {
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.permission_error, Snackbar.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        if (needReload) {
            MainHandler mainHandler = new MainHandler(this);
            mainHandler.sendEmptyMessage(Config.HANDLER_RELOAD_TABLE_DATA);
        }
        activityDestroy = true;
        loadingDialog = null;
        BaseMethod.getApp(this).setCourseActivity(null);
        super.onDestroy();
    }
}
