package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.InfoMethods.ExamMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.LevelExamMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.MoaMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.PersonMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.ScoreMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.SuspendCourseMethod;

/**
 * Created by 10696 on 2018/3/16.
 * 提前缓存数据
 */

public class TempAsync extends AsyncTask<Context, Void, Void> {

    @Nullable
    @Override
    protected Void doInBackground(Context... contexts) {
        try {
            final Context context = contexts[0];
            Thread[] threads = new Thread[6];
            threads[0] = new Thread(new Runnable() {
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
            threads[1] = new Thread(new Runnable() {
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
            threads[2] = new Thread(new Runnable() {
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
            threads[3] = new Thread(new Runnable() {
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
            threads[4] = new Thread(new Runnable() {
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
            threads[5] = new Thread(new Runnable() {
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

            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
