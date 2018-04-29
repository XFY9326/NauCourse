package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import tool.xfy9326.naucourse.Activities.ScoreActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.LevelExamMethod;
import tool.xfy9326.naucourse.Methods.PersonMethod;
import tool.xfy9326.naucourse.Methods.ScoreMethod;
import tool.xfy9326.naucourse.Utils.CourseScore;
import tool.xfy9326.naucourse.Utils.LevelExam;
import tool.xfy9326.naucourse.Utils.StudentLearnProcess;
import tool.xfy9326.naucourse.Utils.StudentScore;

/**
 * Created by 10696 on 2018/3/2.
 */

public class ScoreAsync extends AsyncTask<Context, Void, Context> {
    private int personLoadSuccess = -1;
    private int scoreLoadSuccess = -1;
    private int levelExamLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private StudentScore studentScore;
    @Nullable
    private CourseScore courseScore;
    @Nullable
    private StudentLearnProcess studentLearnProcess;
    @Nullable
    private LevelExam levelExam;

    public ScoreAsync() {
        this.studentScore = null;
        this.courseScore = null;
        this.studentLearnProcess = null;
        this.levelExam = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            int loadTime = 0;
            ScoreActivity scoreActivity = BaseMethod.getApp(context[0]).getScoreActivity();
            if (scoreActivity != null) {
                loadTime = scoreActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                studentScore = (StudentScore) DataMethod.getOfflineData(context[0], StudentScore.class, PersonMethod.FILE_NAME_SCORE);
                studentLearnProcess = (StudentLearnProcess) DataMethod.getOfflineData(context[0], StudentLearnProcess.class, PersonMethod.FILE_NAME_PROCESS);
                courseScore = (CourseScore) DataMethod.getOfflineData(context[0], CourseScore.class, ScoreMethod.FILE_NAME);
                levelExam = (LevelExam) DataMethod.getOfflineData(context[0], LevelExam.class, LevelExamMethod.FILE_NAME);
                personLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                scoreLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                levelExamLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (scoreActivity != null) {
                    scoreActivity.setLoadTime(loadTime);
                }
            } else {
                PersonMethod personMethod = new PersonMethod(context[0]);
                personLoadSuccess = personMethod.load();
                if (personLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    studentScore = personMethod.getUserScore(loadTime > 1);
                    studentLearnProcess = personMethod.getUserProcess(loadTime > 1);
                }

                ScoreMethod scoreMethod = new ScoreMethod(context[0]);
                scoreLoadSuccess = scoreMethod.load();
                if (scoreLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    courseScore = scoreMethod.getCourseScore(loadTime > 1);
                }

                LevelExamMethod levelExamMethod = new LevelExamMethod(context[0]);
                levelExamLoadSuccess = levelExamMethod.load();
                if (levelExamLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    levelExam = levelExamMethod.getLevelExam(loadTime > 1);
                }

                loadTime++;
                scoreActivity.setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        ScoreActivity scoreActivity = BaseMethod.getApp(context).getScoreActivity();
        if (scoreActivity != null) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{personLoadSuccess, scoreLoadSuccess, levelExamLoadSuccess}, loadCode)) {
                scoreActivity.setMainScore(studentScore, studentLearnProcess, courseScore, levelExam);
            }
            scoreActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
