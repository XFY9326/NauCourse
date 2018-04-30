package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.PersonFragment;
import tool.xfy9326.naucourse.Fragments.TableFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.SchoolTimeMethod;
import tool.xfy9326.naucourse.Methods.TableMethod;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by 10696 on 2018/3/2.
 */

public class TableAsync extends AsyncTask<Context, Void, Context> {
    private int tableLoadSuccess = -1;
    private int timeLoadSuccess = -1;
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
        try {
            int loadTime = 0;
            TableFragment tableFragment = BaseMethod.getApp(context[0]).getViewPagerAdapter().getTableFragment();
            if (tableFragment != null) {
                loadTime = tableFragment.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                schoolTime = (SchoolTime) DataMethod.getOfflineData(context[0], SchoolTime.class, SchoolTimeMethod.FILE_NAME);
                course = DataMethod.getOfflineTableData(context[0]);
                tableLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                timeLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (tableFragment != null) {
                    tableFragment.setLoadTime(loadTime);
                }
            } else {
                TableMethod tableMethod = new TableMethod(context[0]);
                tableLoadSuccess = tableMethod.load();
                if (tableLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    course = tableMethod.getCourseTable(loadTime > 1);
                }

                SchoolTimeMethod schoolTimeMethod = new SchoolTimeMethod(context[0]);
                timeLoadSuccess = schoolTimeMethod.load();
                if (timeLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    schoolTime = schoolTimeMethod.getSchoolTime();
                }

                loadTime++;
                tableFragment.setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        TableFragment tableFragment = BaseMethod.getApp(context).getViewPagerAdapter().getTableFragment();
        PersonFragment personFragment = BaseMethod.getApp(context).getViewPagerAdapter().getPersonFragment();
        if (tableFragment != null) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{tableLoadSuccess, timeLoadSuccess}, loadCode)) {
                tableFragment.CourseSet(course, schoolTime, context, false);
            }
            tableFragment.lastViewSet(context);
        }
        if (personFragment != null) {
            personFragment.TimeTextSet(schoolTime, context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
