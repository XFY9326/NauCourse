package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.ExamMethod;
import tool.xfy9326.naucourse.Methods.LevelExamMethod;
import tool.xfy9326.naucourse.Methods.PersonMethod;
import tool.xfy9326.naucourse.Methods.ScoreMethod;
import tool.xfy9326.naucourse.Methods.SuspendCourseMethod;

/**
 * Created by 10696 on 2018/3/16.
 * 提前缓存数据
 */

public class TempAsync extends AsyncTask<Context, Void, Void> {

    @Nullable
    @Override
    protected Void doInBackground(Context... contexts) {
        try {
            ExamMethod examMethod = new ExamMethod(contexts[0]);
            if (examMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                examMethod.saveTemp();
            }
            ScoreMethod scoreMethod = new ScoreMethod(contexts[0]);
            if (scoreMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                scoreMethod.saveTemp();
            }
            PersonMethod personMethod = new PersonMethod(contexts[0]);
            if (personMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                personMethod.saveScoreTemp();
            }
            LevelExamMethod levelExamMethod = new LevelExamMethod(contexts[0]);
            if (levelExamMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                levelExamMethod.saveTemp();
            }
            SuspendCourseMethod suspendCourseMethod = new SuspendCourseMethod(contexts[0]);
            if (suspendCourseMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                suspendCourseMethod.saveTemp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
