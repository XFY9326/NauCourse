package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.TableFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.TableMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by 10696 on 2018/3/2.
 */

public class TableAsync extends AsyncTask<Context, Void, Context> {
    private int tableLoadSuccess = -1;
    private int timeLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    private ArrayList<Course> course;
    private SchoolTime schoolTime;

    public TableAsync() {
        this.course = null;
        this.schoolTime = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            int loadTime = 0;
            TableFragment tableFragment = BaseMethod.getBaseApplication(context[0]).getViewPagerAdapter().getTableFragment();
            if (tableFragment != null) {
                loadTime = tableFragment.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                schoolTime = (SchoolTime) BaseMethod.getOfflineData(context[0], SchoolTime.class, TimeMethod.FILE_NAME);
                course = BaseMethod.getOfflineTableData(context[0]);
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
                    course = tableMethod.getCourseTable();
                }

                TimeMethod timeMethod = new TimeMethod(context[0]);
                timeLoadSuccess = timeMethod.load();
                if (timeLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    schoolTime = timeMethod.getSchoolTime();
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
    protected void onPostExecute(Context context) {
        TableFragment tableFragment = BaseMethod.getBaseApplication(context).getViewPagerAdapter().getTableFragment();
        if (tableFragment != null) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{tableLoadSuccess, timeLoadSuccess}, loadCode)) {
                tableFragment.CourseSet(course, schoolTime, context);
            }
            tableFragment.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
