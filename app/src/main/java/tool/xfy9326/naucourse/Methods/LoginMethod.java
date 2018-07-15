package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.Objects;

import lib.xfy9326.naujwc.NauJwcClient;
import tool.xfy9326.naucourse.Config;

/**
 * Created by xfy9326 on 18-2-20.
 * 登陆登出以及获取数据方法
 */

public class LoginMethod {
    private static boolean isTryingReLogin = false;

    /**
     * 获取教务系统详情数据
     * 必须在非UI线程运行
     *
     * @param context    Context
     * @param url        需要获取的url
     * @param tryReLogin 检测到登陆错误后是否尝试重新登陆
     * @return 获取的数据字符串
     * @throws Exception 网络连接中的错误
     */
    @Nullable
    public static String getData(@NonNull Context context, String url, boolean tryReLogin) throws Exception {
        String data = BaseMethod.getApp(context).getClient().getUserData(url);
        if (!checkUserLogin(Objects.requireNonNull(data)) && tryReLogin) {
            int reLogin_result = reLogin(context);
            switch (reLogin_result) {
                case Config.RE_LOGIN_SUCCESS:
                    data = BaseMethod.getApp(context).getClient().getUserData(url);
                    break;
                case Config.RE_LOGIN_TRYING:
                    while (isTryingReLogin) {
                        Thread.sleep(500);
                    }
                    return getData(context, url, false);
                case Config.RE_LOGIN_FAILED:
                    Log.d("NETWORK", "RE LOGIN ERROR");
                    break;
            }
        }
        System.gc();
        return data;
    }

    /**
     * 检测用用户是否登陆成功
     *
     * @param data 获取的网络数据
     * @return 是否登陆成功
     */
    public static boolean checkUserLogin(String data) {
        return !(data.contains("系统错误提示页") && data.contains("当前程序在执行过程中出现了未知异常，请重试") || data.contains("用户登录_南京审计大学教务管理系统"));
    }

    /**
     * 用户Cookie过期后尝试重新登陆
     *
     * @param context Context
     * @return ReLogin状态值
     * @throws Exception 重新登陆时的网络错误
     */
    private static int reLogin(@NonNull Context context) throws Exception {
        if (!isTryingReLogin) {
            isTryingReLogin = true;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            String pw = SecurityMethod.getUserPassWord(context);
            NauJwcClient nauJwcClient = BaseMethod.getApp(context).getClient();
            nauJwcClient.loginOut();
            Thread.sleep(1000);
            if (nauJwcClient.login(id, Objects.requireNonNull(pw))) {
                String loginURL = nauJwcClient.getLoginUrl();
                if (loginURL != null) {
                    sharedPreferences.edit().putString(Config.PREFERENCE_LOGIN_URL, loginURL).apply();
                    isTryingReLogin = false;
                    return Config.RE_LOGIN_SUCCESS;
                }
            }
            sharedPreferences.edit().putBoolean(Config.PREFERENCE_HAS_LOGIN, false).apply();
            isTryingReLogin = false;
            return Config.RE_LOGIN_FAILED;
        } else {
            return Config.RE_LOGIN_TRYING;
        }
    }

    /**
     * 注销用户登陆
     *
     * @param context Context
     * @return 是否成功注销登陆
     */
    public static boolean loginOut(@NonNull Context context) {
        try {
            BaseMethod.getApp(context).getClient().loginOut();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().remove(Config.PREFERENCE_LOGIN_URL)
                    .remove(Config.PREFERENCE_NETWORK_ACCOUNT)
                    .remove(Config.PREFERENCE_NETWORK_PASSWORD)
                    .remove(Config.PREFERENCE_NETWORK_REMEMBER_PASSWORD)
                    .remove(Config.PREFERENCE_PERSONAL_INFO_LOAD_DATE)
                    .remove(Config.PREFERENCE_COURSE_TABLE_LOAD_DATE)
                    .remove(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND)
                    .remove(Config.PREFERENCE_SCHOOL_CALENDAR_URL)
                    .remove(Config.PREFERENCE_CLASS_BEFORE_NOTIFY)
                    .putBoolean(Config.PREFERENCE_HAS_LOGIN, false)
                    .apply();
            cleanUserTemp(context);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 清空用户缓存的数据
     *
     * @param context Context
     */
    public static void cleanUserTemp(Context context) {
        File cache_file = context.getCacheDir();
        for (File file : cache_file.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        File data_file = context.getFilesDir();
        for (File file : data_file.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

}
