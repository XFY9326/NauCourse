package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

import tool.xfy9326.naucourse.Config;

/**
 * Created by xfy9326 on 18-2-20.
 * 登陆登出以及获取数据方法
 */

public class LoginMethod {

    static String getData(final Context context, final String url) throws Exception {
        String data = BaseMethod.getBaseApplication(context).getClient().getUserData(url);
        System.gc();
        return data;
    }

    static boolean checkUserLogin(String data) {
        return !(data.contains("系统错误提示页") && data.contains("当前程序在执行过程中出现了未知异常，请重试") || data.contains("用户登录_南京审计大学教务管理系统"));
    }

    public static boolean loginOut(Context context) {
        try {
            boolean result = false;
            BaseMethod.getBaseApplication(context).getClient().loginOut();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().remove(Config.PREFERENCE_LOGIN_URL).putBoolean(Config.PREFERENCE_HAS_LOGIN, false).apply();
            File cache_file = context.getCacheDir();
            for (File file : cache_file.listFiles()) {
                result = file.delete();
            }
            File data_file = context.getFilesDir();
            for (File file : data_file.listFiles()) {
                result = file.delete();
            }
            Log.d("LOGIN_OUT_CLEAR", String.valueOf(result));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
