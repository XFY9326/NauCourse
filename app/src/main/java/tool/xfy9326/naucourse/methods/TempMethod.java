package tool.xfy9326.naucourse.methods;

import android.content.Context;

import java.util.concurrent.ExecutorService;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.netInfoMethods.ExamMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.HistoryScoreMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.LevelExamMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.PersonMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.ScoreMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.SuspendCourseMethod;

public class TempMethod {

    public static void loadAllTemp(final Context context) {
        ExecutorService executorService = BaseMethod.getApp(context).getExecutorService();
        executorService.submit(() -> {
            try {
                ExamMethod examMethod = new ExamMethod(context);
                if (examMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                    examMethod.saveTemp();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.submit(() -> {
            try {
                ScoreMethod scoreMethod = new ScoreMethod(context);
                if (scoreMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                    scoreMethod.saveTemp();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.submit(() -> {
            try {
                HistoryScoreMethod historyScoreMethod = new HistoryScoreMethod(context);
                if (historyScoreMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                    historyScoreMethod.saveTemp();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.submit(() -> {
            try {
                PersonMethod personMethod = new PersonMethod(context);
                if (personMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                    personMethod.saveScoreTemp();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.submit(() -> {
            try {
                LevelExamMethod levelExamMethod = new LevelExamMethod(context);
                if (levelExamMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                    levelExamMethod.saveTemp();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.submit(() -> {
            try {
                SuspendCourseMethod suspendCourseMethod = new SuspendCourseMethod(context);
                if (suspendCourseMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                    suspendCourseMethod.saveTemp();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
