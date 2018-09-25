package tool.xfy9326.naucourse.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import tool.xfy9326.naucourse.AsyncTasks.CourseListAsync;
import tool.xfy9326.naucourse.AsyncTasks.CourseNextListAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Handlers.MainHandler;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.CourseEditMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.SchoolTimeMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.TableMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Views.RecyclerViews.CourseAdapter;

public class CourseActivity extends AppCompatActivity {
    public static final int COURSE_EDIT_REQUEST_CODE = 1;
    private static final int COURSE_ADD_REQUEST_CODE = 2;
    public boolean activityDestroy = true;
    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private ArrayList<Course> courseArrayList;
    private int lastOffset = 0;
    private int lastPosition = 0;
    private boolean needSave = false;
    private boolean needReload = false;
    private Dialog loadingDialog = null;

    private String customStartTermDate = null;
    private String customEndTermDate = null;
    private Dialog termSetDialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        activityDestroy = false;
        BaseMethod.getApp(this).setCourseActivity(this);
        ToolBarSet();
        ViewSet();
        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            //清空课程
            case R.id.menu_course_delete_all:
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.confirm_delete_all, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        courseArrayList.clear();
                        courseAdapter.notifyDataSetChanged();
                        needSave = true;
                        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton_course_add);
                        if (floatingActionButton.getVisibility() != View.VISIBLE) {
                            floatingActionButton.setVisibility(View.VISIBLE);
                            ViewCompat.animate(floatingActionButton).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                                    .setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener(null)
                                    .start();
                        }
                    }
                }).show();
                break;
            //学期设定
            case R.id.menu_course_term_date:
                setTermDate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    synchronized private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                courseArrayList = DataMethod.getOfflineTableData(CourseActivity.this);
                if (courseArrayList == null) {
                    courseArrayList = new ArrayList<>();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!activityDestroy) {
                            courseAdapter = new CourseAdapter(CourseActivity.this, courseArrayList);
                            recyclerView.setAdapter(courseAdapter);
                        }
                    }
                });
            }
        }).start();
    }

    private void ViewSet() {
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
        scrollToPosition();

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton_course_add);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);
                builder.setItems(new String[]{getString(R.string.create_new_course), getString(R.string.import_course_from_jwc_current), getString(R.string.import_course_from_jwc_next)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                        }
                    }
                });
                builder.show();
            }
        });

        autoUpdateCourseAlert();
    }

    private void createNewCourse() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.dialog_course_term_set, (ViewGroup) findViewById(R.id.layout_dialog_course_term_set));

        AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);
        builder.setTitle(R.string.add_course_term);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText_year = view.findViewById(R.id.editText_course_school_year);
                RadioButton radioButton_term_one = view.findViewById(R.id.radioButton_term_one);
                String str_year = editText_year.getText().toString();
                if (!str_year.isEmpty() && BaseMethod.isInteger(str_year)) {
                    long year = Long.valueOf(str_year);
                    if (year >= 1983L) {
                        //仅支持四位数的年份，仅支持一年两学期制
                        long term = (year * 10000L + year + 1L) * 10L + (radioButton_term_one.isChecked() ? 1L : 2L);
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
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.show();
    }

    private void autoUpdateCourseAlert() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE)) {
            if (!sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.attention);
                builder.setMessage(R.string.auto_update_course_table_alert);
                builder.setPositiveButton(android.R.string.yes, null);
                builder.setNeutralButton(R.string.no_alert_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPreferences.edit().putBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT, true).apply();
                    }
                });
                builder.show();
            }
        }
    }

    //还原下拉的列表位置
    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) Objects.requireNonNull(recyclerView).getLayoutManager();
        View topView = layoutManager.getChildAt(0);
        if (topView != null) {
            lastOffset = topView.getTop();
            lastPosition = layoutManager.getPosition(topView);
        }
    }

    private void scrollToPosition() {
        if (Objects.requireNonNull(recyclerView).getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }

    @Override
    public void onBackPressed() {
        saveCheck();
    }

    //保存检查
    private void saveCheck() {
        if (needSave) {
            Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.save_attention, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }).show();
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
        showLoadingDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final CourseEditMethod.CourseCheckResult checkResult = CourseEditMethod.checkCourseList(courseArrayList);
                if (checkResult.isHasError()) {
                    final String checkName = checkResult.getCheckCourseName();
                    final String conflictName = checkResult.getConflictCourseName();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (checkName != null && conflictName != null) {
                                Snackbar.make(findViewById(R.id.layout_course_manage_content), getString(R.string.course_edit_error_conflict, checkName, conflictName), Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.course_edit_error, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    DataMethod.saveOfflineData(CourseActivity.this, courseArrayList, TableMethod.FILE_NAME, false);
                    needSave = false;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!activityDestroy) {
                            if (loadingDialog != null && loadingDialog.isShowing()) {
                                loadingDialog.cancel();
                                loadingDialog = null;
                            }
                            if (!checkResult.isHasError()) {
                                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.save_success, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
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
                        for (i = 0; i < courseArrayList.size() && !found; i++) {
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
                            courseAdapter.updateList(courseArrayList);
                        }
                        if (!found) {
                            courseAdapter.notifyItemRangeInserted(i, 1);
                        } else {
                            courseAdapter.notifyItemChanged(i);
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //从教务处导入本学期课程数据
    private void importDataFromJwc() {
        showLoadingDialog();
        new CourseListAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }

    //从教务处导入下学期课程数据
    private void importDataFromJwcNext() {
        showLoadingDialog();
        Toast.makeText(this, R.string.need_custom_term_alert, Toast.LENGTH_LONG).show();
        new CourseNextListAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }

    /**
     * 接受网络导入的数据并显示
     *
     * @param courses       课程数据列表
     * @param isCurrentTerm 是否是当前学期的课程表
     */
    public void addCourseList(final ArrayList<Course> courses, boolean isCurrentTerm, boolean nextTermCourseImportError) {
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
                    builder.setTitle(isCurrentTerm ? R.string.import_course_from_jwc_current : R.string.import_course_from_jwc_next);
                    builder.setMultiChoiceItems(name, checked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            checked[which] = isChecked;
                        }
                    });
                    builder.setPositiveButton(R.string.add_course_and_clean, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            chooseCourseAdd(checked, courses, true);
                        }
                    });
                    builder.setNeutralButton(R.string.add_course_only, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            chooseCourseAdd(checked, courses, false);
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            courses.clear();
                        }
                    });
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

    private void chooseCourseAdd(boolean[] checked, ArrayList<Course> courses, boolean termCheck) {
        ArrayList<Course> courses_choose = new ArrayList<>();
        for (int i = 0; i < checked.length; i++) {
            if (checked[i]) {
                courses_choose.add(courses.get(i));
            }
        }
        courseArrayList = CourseEditMethod.combineCourseList(courses_choose, courseArrayList, termCheck);
        courseAdapter.notifyDataSetChanged();
        needSave = true;
        Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.add_success, Snackbar.LENGTH_SHORT).show();
    }

    private void setTermDate() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        customStartTermDate = sharedPreferences.getString(Config.PREFERENCE_CUSTOM_TERM_START_DATE, null);
        customEndTermDate = sharedPreferences.getString(Config.PREFERENCE_CUSTOM_TERM_END_DATE, null);
        showTermSetDialog(CourseActivity.this);
    }

    private void showTermSetDialog(final Activity activity) {
        final Calendar calendarStart = Calendar.getInstance(Locale.CHINA);
        final Calendar calendarEnd = Calendar.getInstance(Locale.CHINA);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        final SchoolTime schoolTime = (SchoolTime) DataMethod.getOfflineData(activity, SchoolTime.class, SchoolTimeMethod.FILE_NAME);
        if (schoolTime != null) {
            try {
                calendarStart.setTime(simpleDateFormat.parse(schoolTime.getStartTime()));
                calendarEnd.setTime(simpleDateFormat.parse(schoolTime.getEndTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (customStartTermDate != null && customEndTermDate != null) {
            try {
                calendarStart.setTime(simpleDateFormat.parse(customStartTermDate));
                calendarEnd.setTime(simpleDateFormat.parse(customEndTermDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            customStartTermDate = simpleDateFormat.format(calendarStart.getTime());
            customEndTermDate = simpleDateFormat.format(calendarEnd.getTime());
        }

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_term_time_set, (ViewGroup) activity.findViewById(R.id.layout_dialog_term_time_set));

        TextView textView_start = view.findViewById(R.id.textView_custom_start_date);
        textView_start.setText(activity.getString(R.string.custom_term_start_date, customStartTermDate));

        TextView textView_end = view.findViewById(R.id.textView_custom_end_date);
        textView_end.setText(activity.getString(R.string.custom_term_end_date, customEndTermDate));

        Button button_start = view.findViewById(R.id.button_custom_start_date);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (termSetDialog != null) {
                    if (termSetDialog.isShowing()) {
                        termSetDialog.cancel();
                    }
                }
                showDatePickDialog(activity, true, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH));
            }
        });

        Button button_end = view.findViewById(R.id.button_custom_end_date);
        button_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (termSetDialog != null) {
                    if (termSetDialog.isShowing()) {
                        termSetDialog.cancel();
                    }
                }
                showDatePickDialog(activity, false, calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH), calendarEnd.get(Calendar.DAY_OF_MONTH));
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.custom_term);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (customStartTermDate != null && customEndTermDate != null) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                    try {
                        long termDay = simpleDateFormat.parse(customEndTermDate).getTime() - simpleDateFormat.parse(customStartTermDate).getTime();
                        if (termDay <= 0 || termDay / (1000 * 60 * 60 * 24) < 30 || termDay / (1000 * 60 * 60 * 24) > (7 * 24)) {
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
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferences.edit().remove(Config.PREFERENCE_OLD_TERM_START_DATE)
                        .remove(Config.PREFERENCE_OLD_TERM_END_DATE)
                        .remove(Config.PREFERENCE_CUSTOM_TERM_START_DATE)
                        .remove(Config.PREFERENCE_CUSTOM_TERM_END_DATE).apply();
                needReload = true;
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.term_set_success, Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.setView(view);
        termSetDialog = builder.show();
    }

    private void showDatePickDialog(Activity activity, final boolean isStartDate, int year, int month, int day) {
        final DatePickerDialog pickerDialog = new DatePickerDialog(activity, null, year, month, day);
        if (isStartDate) {
            pickerDialog.setTitle(R.string.custom_term_start_date);
        } else {
            pickerDialog.setTitle(R.string.custom_term_end_date);
        }
        pickerDialog.setCancelable(false);
        pickerDialog.setCanceledOnTouchOutside(false);
        pickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatePicker datePicker = pickerDialog.getDatePicker();
                DecimalFormat df = new DecimalFormat("00");
                String getDate = datePicker.getYear() + "-" + df.format(datePicker.getMonth() + 1) + "-" + df.format(datePicker.getDayOfMonth());
                if (isStartDate) {
                    customStartTermDate = getDate;
                } else {
                    customEndTermDate = getDate;
                }
                showTermSetDialog(CourseActivity.this);
            }
        });
        pickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showTermSetDialog(CourseActivity.this);
            }
        });
        pickerDialog.show();
    }

    //显示加载中提示
    private void showLoadingDialog() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_loading, (ViewGroup) findViewById(R.id.dialog_layout_loading));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(view);
        loadingDialog = builder.show();
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
