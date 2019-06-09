package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.SuspendCourseActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.SuspendCourseMethod;
import tool.xfy9326.naucourse.utils.SuspendCourse;

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
                suspendCourse = (SuspendCourse) DataMethod.getOfflineData(context[0], SuspendCourse.class, SuspendCourseMethod.FILE_NAME, SuspendCourseMethod.IS_ENCRYPT);
                loadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                SuspendCourseMethod suspendCourseMethod = new SuspendCourseMethod(context[0]);
                loadSuccess = suspendCourseMethod.load();
                if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    suspendCourse = suspendCourseMethod.getData(loadTime > 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                loadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
            } else {
                loadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (suspendCourseActivity != null) {
            suspendCourseActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            NetMethod.showConnectErrorOnce = false;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        SuspendCourseActivity suspendCourseActivity = BaseMethod.getApp(context).getSuspendCourseActivity();
        if (suspendCourseActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{loadSuccess}, loadCode, false)) {
                suspendCourseActivity.setSuspendCourse(suspendCourse);
            }
            suspendCourseActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
