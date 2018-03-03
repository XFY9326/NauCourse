package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

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
            int loadTime = BaseMethod.getBaseApplication(context[0]).getExamActivity().getLoadTime();
            if (loadTime == 0) {
                //首次只加载离线数据
                exam = (Exam) BaseMethod.getOfflineData(context[0], Exam.class, ExamMethod.FILE_NAME);
                examLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                BaseMethod.getBaseApplication(context[0]).getExamActivity().setLoadTime(loadTime);
            } else {
                ExamMethod examMethod = new ExamMethod(context[0]);
                examLoadSuccess = examMethod.load();
                if (examLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    exam = examMethod.getExam();
                }

                if (examLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    loadTime++;
                    BaseMethod.getBaseApplication(context[0]).getExamActivity().setLoadTime(loadTime);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        if (BaseMethod.checkNetWorkCode(context, new int[]{examLoadSuccess}, loadCode)) {
            BaseMethod.getBaseApplication(context).getExamActivity().setExam(exam);
        }
        BaseMethod.getBaseApplication(context).getExamActivity().lastViewSet(context);
        System.gc();
        super.onPostExecute(context);
    }
}
