package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.fragments.PersonFragment;
import tool.xfy9326.naucourse.fragments.TableFragment;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.CourseEditMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.TimeMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.SchoolTimeMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.TableMethod;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.SchoolTime;
import tool.xfy9326.naucourse.views.ViewPagerAdapter;

public class TableAsync extends AsyncTask<Context, Void, Context> {
    private int timeLoadSuccess = -1;
    private int tableLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private ArrayList<Course> course;
    @Nullable
    private SchoolTime schoolTime;

    public TableAsync() {
        this.course = null;
        this.schoolTime = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            TableFragment tableFragment = viewPagerAdapter.getTableFragment();
            try {
                if (tableFragment != null) {
                    loadTime = tableFragment.getLoadTime();
                }
                course = DataMethod.getOfflineTableData(context[0]);
                tableLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                if (loadTime == 0) {
                    schoolTime = (SchoolTime) DataMethod.getOfflineData(context[0], SchoolTime.class, SchoolTimeMethod.FILE_NAME, SchoolTimeMethod.IS_ENCRYPT);
                    timeLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    schoolTime = TimeMethod.termSetCheck(context[0], schoolTime, false);
                } else {
                    SchoolTimeMethod schoolTimeMethod = new SchoolTimeMethod(context[0]);
                    timeLoadSuccess = schoolTimeMethod.load();
                    if (timeLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        schoolTime = schoolTimeMethod.getSchoolTime();
                        schoolTime = TimeMethod.termSetCheck(context[0], schoolTime, loadTime == 1);
                    }

                    //首次加载没有课程数据时或者打开自动更新时自动联网获取
                    boolean auto_update = PreferenceManager.getDefaultSharedPreferences(context[0]).getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE);
                    if (course == null) {
                        TableMethod tableMethod = new TableMethod(context[0]);
                        tableLoadSuccess = tableMethod.load();
                        if (tableLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                            course = tableMethod.getCourseTable(true);
                        }
                    } else if (auto_update) {
                        TableMethod tableMethod = new TableMethod(context[0]);
                        if (tableMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                            ArrayList<Course> update_course = tableMethod.getCourseTable(false);
                            if (update_course != null) {
                                ArrayList<Course> now_course = CourseEditMethod.combineCourseList(update_course, course, true, false, true);
                                if (now_course != null && !CourseEditMethod.checkCourseList(now_course).isHasError()) {
                                    course.clear();
                                    course.addAll(now_course);
                                    DataMethod.saveOfflineData(context[0], course, TableMethod.FILE_NAME, false, TableMethod.IS_ENCRYPT);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            if (tableFragment != null) {
                tableFragment.setLoadTime(++loadTime);
            }
            if (loadTime > 2) {
                BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
            }
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            TableFragment tableFragment = viewPagerAdapter.getTableFragment();
            PersonFragment personFragment = viewPagerAdapter.getPersonFragment();
            if (tableFragment != null) {
                if (NetMethod.checkNetWorkCode(context, new int[]{timeLoadSuccess, tableLoadSuccess}, loadCode, false)) {
                    tableFragment.CourseSet(course, schoolTime, context, true);
                }
                tableFragment.lastViewSet(context, course == null || schoolTime == null);
            }
            if (personFragment != null) {
                personFragment.TimeTextSet(schoolTime, context);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
