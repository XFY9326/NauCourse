package tool.xfy9326.naucourse.Methods;

import android.content.Context;

import java.util.concurrent.ExecutorService;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.ExamMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.LevelExamMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.MoaMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.PersonMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.ScoreMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.SuspendCourseMethod;

public class TempMethod {

    public static void loadAllTemp(final Context context) {
        ExecutorService executorService = BaseMethod.getApp(context).getExecutorService();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ExamMethod examMethod = new ExamMethod(context);
                    if (examMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                        examMethod.saveTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ScoreMethod scoreMethod = new ScoreMethod(context);
                    if (scoreMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                        scoreMethod.saveTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    PersonMethod personMethod = new PersonMethod(context);
                    if (personMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                        personMethod.saveScoreTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    LevelExamMethod levelExamMethod = new LevelExamMethod(context);
                    if (levelExamMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                        levelExamMethod.saveTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SuspendCourseMethod suspendCourseMethod = new SuspendCourseMethod(context);
                    if (suspendCourseMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                        suspendCourseMethod.saveTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    MoaMethod moaMethod = new MoaMethod(context);
                    if (moaMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                        moaMethod.saveTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
