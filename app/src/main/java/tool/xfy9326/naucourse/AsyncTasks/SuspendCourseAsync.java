package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Activities.SuspendCourseActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.SuspendCourseMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.SuspendCourse;

public class SuspendCourseAsync extends AsyncTask<Context, Void, Context> {
    private int loadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private SuspendCourse suspendCourse;

    public SuspendCourseAsync() {
        this.suspendCourse = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        SuspendCourseActivity suspendCourseActivity = BaseMethod.getApp(context[0]).getSuspendCourseActivity();
        try {
            if (suspendCourseActivity != null) {
                loadTime = suspendCourseActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                suspendCourse = (SuspendCourse) DataMethod.getOfflineData(context[0], SuspendCourse.class, SuspendCourseMethod.FILE_NAME);
                loadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (suspendCourseActivity != null) {
                    suspendCourseActivity.setLoadTime(loadTime);
                }
            } else {
                SuspendCourseMethod suspendCourseMethod = new SuspendCourseMethod(context[0]);
                loadSuccess = suspendCourseMethod.load();
                if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    suspendCourse = suspendCourseMethod.getSuspendCourse(loadTime > 1);
                }
                loadTime++;
                suspendCourseActivity.setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            if (suspendCourseActivity != null) {
                loadTime++;
                suspendCourseActivity.setLoadTime(loadTime);
            }
        }
        if (loadTime > 2) {
            BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        SuspendCourseActivity suspendCourseActivity = BaseMethod.getApp(context).getSuspendCourseActivity();
        if (suspendCourseActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{loadSuccess}, loadCode)) {
                suspendCourseActivity.setSuspendCourse(suspendCourse);
            }
            suspendCourseActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
