package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.async.ScoreActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.HistoryScoreMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.PersonMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.ScoreMethod;
import tool.xfy9326.naucourse.utils.CourseScore;
import tool.xfy9326.naucourse.utils.HistoryScore;
import tool.xfy9326.naucourse.utils.StudentScore;

/**
 * Created by 10696 on 2018/3/2.
 */

public class ScoreAsync extends AsyncTask<Context, Void, Context> {
    private int personLoadSuccess = -1;
    private int scoreLoadSuccess = -1;
    private int historyScoreLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private StudentScore studentScore;
    @Nullable
    private CourseScore courseScore;
    @Nullable
    private HistoryScore historyScore;

    public ScoreAsync() {
        this.studentScore = null;
        this.courseScore = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        ScoreActivity scoreActivity = BaseMethod.getApp(context[0]).getScoreActivity();
        try {
            if (scoreActivity != null) {
                loadTime = scoreActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                studentScore = (StudentScore) DataMethod.getOfflineData(context[0], StudentScore.class, PersonMethod.FILE_NAME_SCORE, PersonMethod.IS_SCORE_ENCRYPT);
                courseScore = (CourseScore) DataMethod.getOfflineData(context[0], CourseScore.class, ScoreMethod.FILE_NAME, ScoreMethod.IS_ENCRYPT);
                historyScore = (HistoryScore) DataMethod.getOfflineData(context[0], HistoryScore.class, HistoryScoreMethod.FILE_NAME, HistoryScoreMethod.IS_ENCRYPT);
                personLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                scoreLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                historyScoreLoadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                PersonMethod personMethod = new PersonMethod(context[0]);
                personLoadSuccess = personMethod.load();
                if (personLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    studentScore = personMethod.getUserScore(loadTime > 1);
                }

                ScoreMethod scoreMethod = new ScoreMethod(context[0]);
                scoreLoadSuccess = scoreMethod.load();
                if (scoreLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    courseScore = scoreMethod.getData(loadTime > 1);
                }

                HistoryScoreMethod historyScoreMethod = new HistoryScoreMethod(context[0]);
                historyScoreLoadSuccess = historyScoreMethod.load();
                if (historyScoreLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    historyScore = historyScoreMethod.getData(loadTime > 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                personLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                scoreLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                historyScoreLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
            } else {
                personLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                scoreLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                historyScoreLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (scoreActivity != null) {
            scoreActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            NetMethod.showConnectErrorOnce = false;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        ScoreActivity scoreActivity = BaseMethod.getApp(context).getScoreActivity();
        if (scoreActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{personLoadSuccess, scoreLoadSuccess, historyScoreLoadSuccess}, loadCode, false)) {
                scoreActivity.setStudentScore(studentScore, courseScore, historyScore);
            }
            scoreActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
