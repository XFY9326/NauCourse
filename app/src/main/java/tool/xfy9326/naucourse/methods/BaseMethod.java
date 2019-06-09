package tool.xfy9326.naucourse.methods;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Random;
import java.util.regex.Pattern;

import tool.xfy9326.naucourse.BaseApplication;
import tool.xfy9326.naucourse.BuildConfig;
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

    public static void showNewVersionInfo(Context context, boolean versionCheck) {
        boolean showDialog = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (versionCheck) {
            int newVersionInfo = sharedPreferences.getInt(Config.PREFERENCE_NEW_VERSION_INFO, Config.DEFAULT_PREFERENCE_NEW_VERSION_INFO);
            showDialog = newVersionInfo < BuildConfig.VERSION_CODE;
        }
        if (!versionCheck || showDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.new_version_info_title);
            builder.setMessage(R.string.new_version_info_content);
            builder.setPositiveButton(android.R.string.yes, null);
            builder.show();
            if (versionCheck) {
                sharedPreferences.edit().putInt(Config.PREFERENCE_NEW_VERSION_INFO, BuildConfig.VERSION_CODE).apply();
            }
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
            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    /**
     * 显示加载中的提示
     *
     * @param activity       Activity
     * @param cancelable     是否可以取消显示
     * @param cancelListener 对取消显示的监听
     * @return show方法返回的Dialog
     */
    public static Dialog showLoadingDialog(@NonNull Activity activity, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_loading, activity.findViewById(R.id.dialog_layout_loading));
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(cancelable);
        builder.setOnCancelListener(cancelListener);
        builder.setView(view);
        return builder.show();
    }

    public static void hideKeyBoard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static int getRandomColor(Context context) {
        //颜色随机
        int[] colorList = BaseMethod.getColorArray(context);
        Random random = new Random();
        int num = random.nextInt(colorList.length) % (colorList.length + 1);
        return colorList[num];
    }
}
