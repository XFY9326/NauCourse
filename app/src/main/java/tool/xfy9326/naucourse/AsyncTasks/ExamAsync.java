package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import tool.xfy9326.naucourse.Activities.ExamActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.ExamMethod;
import tool.xfy9326.naucourse.Utils.Exam;

/**
 * Created by 10696 on 2018/3/3.
 */

public class ExamAsync extends AsyncTask<Context, Void, Context> {
    private int examLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    private Exam exam;

    public ExamAsync() {
        this.exam = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            int loadTime = 0;
            ExamActivity examActivity = BaseMethod.getBaseApplication(context[0]).getExamActivity();
            if (examActivity != null) {
                loadTime = examActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                exam = (Exam) BaseMethod.getOfflineData(context[0], Exam.class, ExamMethod.FILE_NAME);
                examLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (examActivity != null) {
                    examActivity.setLoadTime(loadTime);
                }
            } else {
                ExamMethod examMethod = new ExamMethod(context[0]);
                examLoadSuccess = examMethod.load();
                if (examLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    exam = examMethod.getExam();
                }

                loadTime++;
                BaseMethod.getBaseApplication(context[0]).getExamActivity().setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        ExamActivity examActivity = BaseMethod.getBaseApplication(context).getExamActivity();
        if (examActivity != null) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{examLoadSuccess}, loadCode)) {
                examActivity.setExam(exam);
            }
            examActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
