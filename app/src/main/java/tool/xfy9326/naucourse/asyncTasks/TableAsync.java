package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.net.SocketTimeoutException;
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
import tool.xfy9326.naucourse.views.MainViewPagerAdapter;

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
        MainViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
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
                        schoolTime = schoolTimeMethod.getData(false);
                        schoolTime = TimeMethod.termSetCheck(context[0], schoolTime, loadTime == 1);
                    }

                    //首次加载没有课程数据时或者打开自动更新时自动联网获取
                    if (course == null || course.size() == 0) {
                        TableMethod tableMethod = new TableMethod(context[0]);
                        tableLoadSuccess = tableMethod.load();
                        if (tableLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                            course = tableMethod.getData(true);
                        }
                    } else if (PreferenceManager.getDefaultSharedPreferences(context[0]).getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE)) {
                        TableMethod tableMethod = new TableMethod(context[0]);
                        if (tableMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                            ArrayList<Course> updateCourse = tableMethod.getData(false);
                            if (updateCourse != null) {
                                ArrayList<Course> nowCourse = CourseEditMethod.combineCourseList(updateCourse, course, true, false, true);
                                if (nowCourse != null && !CourseEditMethod.checkCourseList(nowCourse).isHasError()) {
                                    course.clear();
                                    course.addAll(nowCourse);
                                    DataMethod.saveOfflineData(context[0], course, TableMethod.FILE_NAME, false, TableMethod.IS_ENCRYPT);
                                }
                            }
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                tableLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            } catch (Exception e) {
                e.printStackTrace();
                tableLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            if (tableFragment != null) {
                tableFragment.setLoadTime(++loadTime);
            }
            if (loadTime > 2) {
                NetMethod.showConnectErrorOnce = false;
            }
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        MainViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            TableFragment tableFragment = viewPagerAdapter.getTableFragment();
            PersonFragment personFragment = viewPagerAdapter.getPersonFragment();
            if (tableFragment != null) {
                if (NetMethod.checkNetWorkCode(context, new int[]{timeLoadSuccess, tableLoadSuccess}, loadCode, false)) {
                    tableFragment.courseSet(course, schoolTime, context, true);
                }
                tableFragment.lastViewSet(context, course == null || schoolTime == null);
            }
            if (personFragment != null) {
                personFragment.timeTextSet(schoolTime, context);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
