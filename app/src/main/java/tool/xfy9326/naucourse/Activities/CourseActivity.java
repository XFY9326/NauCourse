package tool.xfy9326.naucourse.Activities;

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

import java.util.ArrayList;
import java.util.Objects;

import tool.xfy9326.naucourse.AsyncTasks.CourseListAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Handlers.MainHandler;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.CourseEditMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.TableMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;
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
                builder.setItems(new String[]{getString(R.string.create_new_course), getString(R.string.import_course_from_jwc_current)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CourseActivity.this, CourseEditActivity.class);
                        if (which == 0) {
                            intent.putExtra(Config.INTENT_ADD_COURSE, true);
                            startActivityForResult(intent, COURSE_ADD_REQUEST_CODE);
                        } else if (which == 1) {
                            importDataFromJwc();
                        }
                    }
                });
                builder.show();
            }
        });

        autoUpdateCourseAlert();
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
                            if (loadingDialog != null) {
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

    //从教务处导入课程数据
    private void importDataFromJwc() {
        showLoadingDialog();
        new CourseListAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }

    /**
     * 接受网络导入的数据并显示
     *
     * @param courses 课程数据列表
     */
    public void addCourseList(final ArrayList<Course> courses) {
        if (!activityDestroy) {
            if (loadingDialog != null) {
                loadingDialog.cancel();
            }

            if (courses != null && courses.size() != 0) {
                String[] name = new String[courses.size()];
                final boolean[] checked = new boolean[courses.size()];
                for (int i = 0; i < courses.size(); i++) {
                    name[i] = courses.get(i).getCourseName();
                    checked[i] = true;
                }

                final ArrayList<Course> courses_choose = new ArrayList<>();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.import_course_from_jwc_current);
                builder.setMultiChoiceItems(name, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which] = isChecked;
                    }
                });
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checked.length; i++) {
                            if (checked[i]) {
                                courses_choose.add(courses.get(i));
                            }
                        }
                        courseArrayList = CourseEditMethod.combineCourseList(courses_choose, courseArrayList);
                        courseAdapter.notifyDataSetChanged();
                        needSave = true;
                        Snackbar.make(findViewById(R.id.layout_course_manage_content), R.string.add_success, Snackbar.LENGTH_SHORT).show();
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
        BaseMethod.getApp(this).setCourseActivity(null);
        super.onDestroy();
    }
}
