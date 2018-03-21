package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

import lib.xfy9326.naujwc.NauJwcClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Tools.AES;

/**
 * Created by xfy9326 on 18-2-20.
 * 登陆登出以及获取数据方法
 */

public class LoginMethod {

    //获取教务系统详情数据
    static String getData(Context context, String url) throws Exception {
        String data = BaseMethod.getApp(context).getClient().getUserData(url);
        if (!checkUserLogin(data)) {
            if (reLogin(context)) {
                data = BaseMethod.getApp(context).getClient().getUserData(url);
            } else {
                Log.d("DATA", "LOGIN ERROR");
            }
        }
        System.gc();
        return data;
    }

    //用户登陆成功检测
    static boolean checkUserLogin(String data) {
        return !(data.contains("系统错误提示页") && data.contains("当前程序在执行过程中出现了未知异常，请重试") || data.contains("用户登录_南京审计大学教务管理系统"));
    }

    //用户cookie过期后自动尝试重登录
    synchronized private static boolean reLogin(Context context) throws Exception {
        Log.d("DATA", "TRY LOGIN");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        String pw = sharedPreferences.getString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW);
        pw = AES.decrypt(pw, id);
        NauJwcClient nauJwcClient = BaseMethod.getApp(context).getClient();
        nauJwcClient.loginOut();
        if (nauJwcClient.login(id, pw)) {
            String loginURL = nauJwcClient.getLoginUrl();
            if (loginURL != null) {
                sharedPreferences.edit().putString(Config.PREFERENCE_LOGIN_URL, loginURL).apply();
                return true;
            }
        }
        sharedPreferences.edit().putBoolean(Config.PREFERENCE_HAS_LOGIN, false).apply();
        return false;
    }

    //注销登陆
    public static boolean loginOut(Context context) {
        try {
            BaseMethod.getApp(context).getClient().loginOut();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().remove(Config.PREFERENCE_LOGIN_URL).putBoolean(Config.PREFERENCE_HAS_LOGIN, false).apply();
            cleanUserTemp(context);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //清空用户缓存数据
    public static void cleanUserTemp(Context context) {
        File cache_file = context.getCacheDir();
        for (File file : cache_file.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        File data_file = context.getFilesDir();
        for (File file : data_file.listFiles()) {
            if (file.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

}
