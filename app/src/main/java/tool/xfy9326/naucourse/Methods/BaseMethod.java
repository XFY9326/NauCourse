package tool.xfy9326.naucourse.Methods;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
     * 判断字符串是否是数字
     *
     * @param str 字符串
     * @return 是否是数字
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

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
     * 获取默认课程颜色数组
     *
     * @param context Context
     * @return 颜色数组
     */
    public static int[] getColorArray(Context context) {
        String[] colorStrList = context.getResources().getStringArray(R.array.default_course_color);
        int[] colorList = new int[colorStrList.length];
        for (int i = 0; i < colorStrList.length; i++) {
            colorList[i] = Color.parseColor(colorStrList[i]);
        }
        return colorList;
    }

    public static void setRefreshing(final SwipeRefreshLayout swipeRefreshLayout, final boolean refreshing) {
        if (swipeRefreshLayout != null && (refreshing && !swipeRefreshLayout.isRefreshing() || !refreshing && swipeRefreshLayout.isRefreshing())) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(refreshing);
                }
            });
        }
    }
}
