package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.async.ExamActivity;
import tool.xfy9326.naucourse.beans.exam.Exam;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.async.ExamMethod;
import tool.xfy9326.naucourse.methods.io.DataMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;

/**
 * Created by 10696 on 2018/3/3.
 */

public class ExamAsync extends AsyncTask<Context, Void, Context> {
    private int examLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private Exam exam;

    public ExamAsync() {
        this.exam = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        ExamActivity examActivity = BaseMethod.getApp(context[0]).getExamActivity();
        try {
            if (examActivity != null) {
                loadTime = examActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                exam = (Exam) DataMethod.getOfflineData(context[0], Exam.class, ExamMethod.FILE_NAME, ExamMethod.IS_ENCRYPT);
                examLoadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                ExamMethod examMethod = new ExamMethod(context[0]);
                examLoadSuccess = examMethod.load();
                if (examLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    exam = examMethod.getData(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                examLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
            } else {
                examLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (examActivity != null) {
            examActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            NetMethod.showConnectErrorOnce = false;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        ExamActivity examActivity = BaseMethod.getApp(context).getExamActivity();
        if (examActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{examLoadSuccess}, loadCode, false)) {
                examActivity.setExam(exam);
            }
            examActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
