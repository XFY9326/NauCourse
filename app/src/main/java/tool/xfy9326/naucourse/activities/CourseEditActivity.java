package tool.xfy9326.naucourse.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.CourseEditMethod;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.CourseDetail;
import tool.xfy9326.naucourse.views.recyclerAdapters.CourseEditAdapter;

public class CourseEditActivity extends AppCompatActivity {
    private boolean needSave = false;
    private Course course;
    private ArrayList<CourseDetail> courseDetailArrayList;
    private CourseEditAdapter courseEditAdapter;
    private Dialog loadingDialog = null;
    private boolean activityDestroy = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_edit);
        activityDestroy = false;
        ToolBarSet();
        getData();
        ViewSet();
    }

    private void ToolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //获取课程数据
    private void getData() {
        Intent intent = getIntent();
        if (intent.hasExtra(Config.INTENT_ADD_COURSE)) {
            course = new Course();
            //自定义课程的课程编号
            course.setCourseId(Config.CUSTOM_COURSE_PREFIX + "-" + System.currentTimeMillis());
            //学期设置
            if (intent.hasExtra(Config.INTENT_ADD_COURSE_TERM)) {
                course.setCourseTerm(String.valueOf(intent.getLongExtra(Config.INTENT_ADD_COURSE_TERM, 0)));
            }
        } else if (intent.hasExtra(Config.INTENT_EDIT_COURSE)) {
            course = (Course) intent.getSerializableExtra(Config.INTENT_EDIT_COURSE_ITEM);
            if (course == null) {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void ViewSet() {
        if (course != null) {
            courseDetailArrayList = getCourseDetailArrayList();
            TextInputEditText editTextCourseEditName = findViewById(R.id.editText_course_edit_name);
            TextInputEditText editTextCourseEditTeacher = findViewById(R.id.editText_course_edit_teacher);
            TextInputEditText editTextCourseEditType = findViewById(R.id.editText_course_edit_type);
            TextInputEditText editTextCourseEditScore = findViewById(R.id.editText_course_edit_score);
            TextInputEditText editTextCourseEditClass = findViewById(R.id.editText_course_edit_class);
            TextInputEditText editTextCourseEditCombineClass = findViewById(R.id.editText_course_edit_combine_class);

            if (course.getCourseName() != null) {
                editTextCourseEditName.setText(course.getCourseName());
            }
            if (course.getCourseTeacher() != null) {
                editTextCourseEditTeacher.setText(course.getCourseTeacher());
            }
            if (course.getCourseType() != null) {
                editTextCourseEditType.setText(course.getCourseType());
            }
            if (course.getCourseScore() != null) {
                editTextCourseEditScore.setText(course.getCourseScore());
            }
            if (course.getCourseClass() != null) {
                editTextCourseEditClass.setText(course.getCourseClass());
            }
            if (course.getCourseCombinedClass() != null) {
                editTextCourseEditCombineClass.setText(course.getCourseCombinedClass());
            }
            //仅在学期正确时显示
            long courseTerm = Long.valueOf(course.getCourseTerm());
            if (courseTerm > 0) {
                //仅支持四位数的年份
                ((TextView) findViewById(R.id.textView_course_edit_term)).setText(getString(R.string.course_edit_course_time_detail, courseTerm / (10L * 10000L), (courseTerm % (10L * 10000L)) / 10L, courseTerm % 10L));
            }

            RecyclerView recyclerView = findViewById(R.id.recyclerView_course_edit_list);
            recyclerView.setFocusableInTouchMode(false);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            courseEditAdapter = new CourseEditAdapter(this, courseDetailArrayList);
            recyclerView.setAdapter(courseEditAdapter);

            FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton_course_edit_add);
            floatingActionButton.setOnClickListener(v -> {
                courseDetailArrayList.add(new CourseDetail());
                courseEditAdapter.setData(courseDetailArrayList);
                courseEditAdapter.notifyItemRangeInserted(courseDetailArrayList.size() - 1, 1);
                needSave = true;
            });
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (item.getItemId()) {
            case R.id.menu_course_save:
                if (sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE)) {
                    if (course.getCourseId() != null && !course.getCourseId().contains(Config.CUSTOM_COURSE_PREFIX) && !course.getCourseId().contains(Config.SEARCH_COURSE_PREFIX)) {
                        autoUpdateCourseAlert();
                        break;
                    }
                }
                saveData();
                break;
            case android.R.id.home:
                saveCheck();
                return true;
            case R.id.menu_course_delete_all:
                Snackbar.make(findViewById(R.id.layout_course_edit_content), R.string.confirm_delete_all, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, v -> {
                    courseDetailArrayList.clear();
                    courseEditAdapter.notifyDataSetChanged();
                    needSave = true;
                    FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton_course_edit_add);
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
                setTerm();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void autoUpdateCourseAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CourseEditActivity.this);
        builder.setTitle(R.string.attention);
        builder.setMessage(R.string.auto_update_course_table_alert);
        builder.setPositiveButton(android.R.string.yes, null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        saveCheck();
    }

    private void saveCheck() {
        if (needSave) {
            Snackbar.make(findViewById(R.id.layout_course_edit_content), R.string.save_attention, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, v -> finish()).show();
        } else {
            finish();
        }
    }

    private void setTerm() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.dialog_course_term_set, findViewById(R.id.layout_dialog_course_term_set));

        final EditText editText_year = view.findViewById(R.id.editText_course_school_year);
        final RadioButton radioButton_term_one = view.findViewById(R.id.radioButton_term_one);
        final RadioButton radioButton_term_two = view.findViewById(R.id.radioButton_term_two);
        long courseTerm = Long.valueOf(course.getCourseTerm());
        if (courseTerm > 0) {
            editText_year.setText(String.valueOf(courseTerm / (10 * 10000)));
            if (courseTerm % 10 == 1) {
                radioButton_term_one.setChecked(true);
                radioButton_term_two.setChecked(false);
            } else if (courseTerm % 10 == 2) {
                radioButton_term_one.setChecked(false);
                radioButton_term_two.setChecked(true);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CourseEditActivity.this);
        builder.setTitle(R.string.course_term);
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            String str_year = editText_year.getText().toString();
            if (!str_year.isEmpty() && BaseMethod.isInteger(str_year)) {
                long year = Long.valueOf(str_year);
                if (year >= 1983L) {
                    //仅支持四位数的年份，仅支持一年两学期制
                    long term = (year * 10000L + year + 1L) * 10L + (radioButton_term_one.isChecked() ? 1L : 2L);
                    course.setCourseTerm(String.valueOf(term));
                    ((TextView) findViewById(R.id.textView_course_edit_term)).setText(getString(R.string.course_edit_course_time_detail, year, year + 1, (radioButton_term_one.isChecked() ? 1 : 2)));
                    needSave = true;
                } else {
                    Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.input_error, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.input_error, Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.show();
    }

    //保存编辑的数据
    synchronized private void saveData() {
        loadingDialog = BaseMethod.showLoadingDialog(this, false, null);
        TextInputEditText editTextCourseEditName = findViewById(R.id.editText_course_edit_name);
        TextInputEditText editTextCourseEditTeacher = findViewById(R.id.editText_course_edit_teacher);
        TextInputEditText editTextCourseEditType = findViewById(R.id.editText_course_edit_type);
        TextInputEditText editTextCourseEditScore = findViewById(R.id.editText_course_edit_score);
        TextInputEditText editTextCourseEditClass = findViewById(R.id.editText_course_edit_class);
        TextInputEditText editTextCourseEditCombineClass = findViewById(R.id.editText_course_edit_combine_class);

        Editable editable_name = editTextCourseEditName.getText();
        Editable editable_teacher = editTextCourseEditTeacher.getText();
        Editable editable_type = editTextCourseEditType.getText();
        Editable editable_score = editTextCourseEditScore.getText();
        Editable editable_class = editTextCourseEditClass.getText();
        Editable editable_combineClass = editTextCourseEditCombineClass.getText();

        if (editable_name != null && editable_teacher != null && editable_type != null && editable_score != null && editable_class != null && editable_combineClass != null) {
            course.setCourseName(editable_name.toString());
            course.setCourseTeacher(editable_teacher.toString());
            course.setCourseType(editable_type.toString());
            course.setCourseScore(editable_score.toString());
            course.setCourseClass(editable_class.toString());
            course.setCourseCombinedClass(editable_combineClass.toString());

            new Thread(() -> {
                course.setCourseDetail(courseDetailArrayList.toArray(new CourseDetail[0]));

                final boolean checkDataCorrect = CourseEditMethod.checkCourseDetail(course.getCourseDetail());
                final boolean checkDataExist = checkAllSet();

                runOnUiThread(() -> {
                    if (!activityDestroy) {
                        if (checkDataCorrect && checkDataExist) {
                            setResult(RESULT_OK, new Intent().putExtra(Config.INTENT_EDIT_COURSE_ITEM, course));
                            needSave = false;
                            Snackbar.make(findViewById(R.id.layout_course_edit_content), R.string.save_success, Snackbar.LENGTH_SHORT).show();
                        } else if (!checkDataCorrect) {
                            Snackbar.make(findViewById(R.id.layout_course_edit_content), R.string.conflict_error, Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(findViewById(R.id.layout_course_edit_content), R.string.input_less, Snackbar.LENGTH_SHORT).show();
                        }

                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.cancel();
                            loadingDialog = null;
                        }
                    }
                });
            }).start();
        }
    }

    //检查必要项目是否已经全部设置
    private boolean checkAllSet() {
        if (course != null
                && course.getCourseId() != null
                && course.getCourseName() != null
                && course.getCourseTeacher() != null
                && course.getCourseDetail() != null
                && course.getCourseDetail().length > 0) {
            for (CourseDetail courseDetail : course.getCourseDetail()) {
                if (!(courseDetail.getWeeks() != null
                        && courseDetail.getWeekMode() != 0
                        && courseDetail.getLocation() != null
                        && courseDetail.getWeekDay() != 0
                        && courseDetail.getCourseTime() != null)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void setArrayChanged() {
        needSave = true;
    }

    //获取课程详细信息的列表
    private ArrayList<CourseDetail> getCourseDetailArrayList() {
        if (course == null) {
            getData();
        }
        if (course != null) {
            CourseDetail[] courseDetails = course.getCourseDetail();
            if (courseDetails != null) {
                courseDetailArrayList = new ArrayList<>(Arrays.asList(courseDetails));
            } else {
                courseDetailArrayList = new ArrayList<>();
                courseDetailArrayList.add(new CourseDetail());
            }
        }
        return courseDetailArrayList;
    }

    @Override
    protected void onDestroy() {
        activityDestroy = true;
        loadingDialog = null;
        super.onDestroy();
    }
}
