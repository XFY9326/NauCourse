package tool.xfy9326.naucourse.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.CourseEditMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;
import tool.xfy9326.naucourse.Views.RecyclerViews.CourseEditAdapter;

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
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
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
            course.setCourseId("Custom-" + System.currentTimeMillis());
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
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                courseDetailArrayList.add(new CourseDetail());
                courseEditAdapter.setData(courseDetailArrayList);
                courseEditAdapter.notifyItemRangeInserted(courseDetailArrayList.size() - 1, 1);
                needSave = true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_course_save:
                saveData();
                break;
            case android.R.id.home:
                saveCheck();
                return true;
            case R.id.menu_course_delete_all:
                Snackbar.make(findViewById(R.id.layout_course_edit_content), R.string.confirm_delete_all, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                    }
                }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveCheck();
    }

    private void saveCheck() {
        if (needSave) {
            Snackbar.make(findViewById(R.id.layout_course_edit_content), R.string.save_attention, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }).show();
        } else {
            finish();
        }
    }

    //保存编辑的数据
    synchronized private void saveData() {
        showLoadingDialog();
        TextInputEditText editTextCourseEditName = findViewById(R.id.editText_course_edit_name);
        TextInputEditText editTextCourseEditTeacher = findViewById(R.id.editText_course_edit_teacher);
        TextInputEditText editTextCourseEditType = findViewById(R.id.editText_course_edit_type);
        TextInputEditText editTextCourseEditScore = findViewById(R.id.editText_course_edit_score);
        TextInputEditText editTextCourseEditClass = findViewById(R.id.editText_course_edit_class);
        TextInputEditText editTextCourseEditCombineClass = findViewById(R.id.editText_course_edit_combine_class);

        course.setCourseName(editTextCourseEditName.getText().toString());
        course.setCourseTeacher(editTextCourseEditTeacher.getText().toString());
        course.setCourseType(editTextCourseEditType.getText().toString());
        course.setCourseScore(editTextCourseEditScore.getText().toString());
        course.setCourseClass(editTextCourseEditClass.getText().toString());
        course.setCourseCombinedClass(editTextCourseEditCombineClass.getText().toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                course.setCourseDetail(courseDetailArrayList.toArray(new CourseDetail[courseDetailArrayList.size()]));

                final boolean checkDataCorrect = CourseEditMethod.checkCourseDetail(course.getCourseDetail());
                final boolean checkDataExist = checkAllSet();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                    }
                });
            }
        }).start();
    }

    //检查必要项目是否已经全部设置
    private boolean checkAllSet() {
        if (course.getCourseId() != null
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
        CourseDetail[] courseDetails = course.getCourseDetail();
        if (courseDetails != null) {
            courseDetailArrayList = new ArrayList<>(Arrays.asList(courseDetails));
        } else {
            courseDetailArrayList = new ArrayList<>();
            courseDetailArrayList.add(new CourseDetail());
        }
        return courseDetailArrayList;
    }

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
        activityDestroy = true;
        loadingDialog = null;
        super.onDestroy();
    }
}
