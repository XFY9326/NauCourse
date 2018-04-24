package tool.xfy9326.naucourse.Methods;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

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
     * 网络连接情况检测以及错误提示
     *
     * @param context         Context
     * @param dataLoadCode    单个数据请求错误代码
     * @param contentLoadCode 整体网络请求错误代码
     * @return 网络检查是否通过
     */
    public static boolean checkNetWorkCode(@NonNull Context context, @NonNull int[] dataLoadCode, int contentLoadCode) {
        if (contentLoadCode == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) {
            Toast.makeText(context, R.string.network_get_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int code : dataLoadCode) {
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN) {
                if (!getApp(context).isShowLoginErrorOnce()) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    getApp(context).setShowLoginErrorOnce();
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA) {
                if (!getApp(context).isShowLoginErrorOnce()) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    getApp(context).setShowLoginErrorOnce();
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR) {
                Toast.makeText(context, R.string.data_get_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
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

}
