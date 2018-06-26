package tool.xfy9326.naucourse.Methods;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.concurrent.Executor;

import tool.xfy9326.naucourse.BaseApplication;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;

/**
 * Created by xfy9326 on 18-2-20.
 * 基础杂项方法
 */

public class BaseMethod {
    private static long DoubleClickTime = 0;

    /**
     * 双击退出
     *
     * @param activity 需要退出的Activity
     */
    public static void doubleClickExit(@NonNull Activity activity) {
        long time = System.currentTimeMillis();
        if (time - DoubleClickTime > 1200) {
            DoubleClickTime = time;
            if (!activity.isDestroyed()) {
                Toast.makeText(activity, activity.getString(R.string.double_click_exit), Toast.LENGTH_SHORT).show();
            }
        } else {
            DoubleClickTime = time;
            activity.finish();
        }
    }

    /**
     * 获取BaseApplication对象
     *
     * @param context Context
     * @return BaseApplication对象
     */
    @NonNull
    public static BaseApplication getApp(Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

    /**
     * 是否数据自动更新
     *
     * @param context Context
     * @return 是否数据自动更新
     */
    public static boolean isDataAutoUpdate(Context context) {
        boolean autoUpdate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_UPDATE_DATA_ON_START, Config.DEFAULT_PREFERENCE_UPDATE_DATA_ON_START);
        if (isDataWifiAutoUpdate(context)) {
            return NetMethod.isWifiNetWork(context) && autoUpdate;
        } else {
            return autoUpdate;
        }
    }

    /**
     * 是否Wifi下数据自动更新
     *
     * @param context Context
     * @return 是否Wifi下数据自动更新
     */
    private static boolean isDataWifiAutoUpdate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_ONLY_UPDATE_UNDER_WIFI, Config.DEFAULT_PREFERENCE_ONLY_UPDATE_UNDER_WIFI);
    }

    /**
     * 获取AsyncTask的线程池
     *
     * @param loadTime 加载的次数
     * @return 线程池
     */
    public static Executor getAsyncTaskExecutor(int loadTime) {
        Executor executor = AsyncTask.SERIAL_EXECUTOR;
        if (loadTime == 0) {
            executor = AsyncTask.THREAD_POOL_EXECUTOR;
        }
        return executor;
    }
}
