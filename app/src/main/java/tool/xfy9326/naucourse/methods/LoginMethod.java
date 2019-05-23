package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;

/**
 * Created by xfy9326 on 18-2-20.
 * 登陆登出以及获取数据方法
 */

public class LoginMethod {
    static boolean isTryingReLogin = false;

    /**
     * 检测用用户是否登陆成功
     *
     * @param data 获取的网络数据
     * @return 是否登陆成功
     */
    public static boolean checkUserLogin(String data) {
        return !(data.contains("系统错误提示页") && data.contains("当前程序在执行过程中出现了未知异常，请重试") || data.contains("用户登录_南京审计大学教务管理系统") || data.contains("南京审计大学统一身份认证登录") || data.contains("location=\"LOGIN.ASPX\";"));
    }

    /**
     * 用户Cookie过期后尝试重新登陆
     *
     * @param context Context
     * @return ReLogin状态值
     * @throws Exception 重新登陆时的网络错误
     */
    static int reLogin(@NonNull Context context) throws Exception {
        if (!isTryingReLogin) {
            isTryingReLogin = true;
            int result = doReLogin(context);
            isTryingReLogin = false;
            return result;
        } else {
            return Config.RE_LOGIN_TRYING;
        }
    }

    synchronized private static int doReLogin(@NonNull Context context) throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        String pw = SecurityMethod.getUserPassWord(context);
        if (!pw.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_PW) && !id.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_ID)) {
            NauSSOClient nauSSOClient = BaseMethod.getApp(context).getClient();
            nauSSOClient.jwcLoginOut();
            Thread.sleep(1000);
            if (nauSSOClient.login(id, Objects.requireNonNull(pw))) {
                nauSSOClient.alstuLogin();
                String loginURL = nauSSOClient.getJwcLoginUrl();
                if (loginURL != null) {
                    sharedPreferences.edit().putString(Config.PREFERENCE_LOGIN_URL, loginURL).apply();
                    return Config.RE_LOGIN_SUCCESS;
                }
            }
        }
        sharedPreferences.edit().putBoolean(Config.PREFERENCE_HAS_LOGIN, false).apply();
        return Config.RE_LOGIN_FAILED;
    }

    /**
     * 注销用户登陆
     *
     * @param context Context
     * @return 是否成功注销登陆
     */
    public static boolean loginOut(@NonNull Context context) {
        try {
            BaseMethod.getApp(context).getClient().jwcLoginOut();
            BaseMethod.getApp(context).getClient().loginOut();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().remove(Config.PREFERENCE_LOGIN_URL)
                    .remove(Config.PREFERENCE_LOGIN_URL)
                    .remove(Config.PREFERENCE_NETWORK_ACCOUNT)
                    .remove(Config.PREFERENCE_NETWORK_PASSWORD)
                    .remove(Config.PREFERENCE_NETWORK_REMEMBER_PASSWORD)
                    .remove(Config.PREFERENCE_PERSONAL_INFO_LOAD_DATE_TIME)
                    .remove(Config.PREFERENCE_COURSE_TABLE_AUTO_LOAD_DATE_TIME)
                    .remove(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND)
                    .remove(Config.PREFERENCE_SCHOOL_CALENDAR_URL)
                    .remove(Config.PREFERENCE_CLASS_BEFORE_NOTIFY)
                    .remove(Config.PREFERENCE_CUSTOM_TERM_START_DATE)
                    .remove(Config.PREFERENCE_CUSTOM_TERM_END_DATE)
                    .remove(Config.PREFERENCE_OLD_TERM_START_DATE)
                    .remove(Config.PREFERENCE_OLD_TERM_END_DATE)
                    .remove(Config.PREFERENCE_AUTO_CHECK_UPDATE)
                    .remove(Config.PREFERENCE_INFO_CHANNEL_SELECTED_JW)
                    .remove(Config.PREFERENCE_INFO_CHANNEL_SELECTED_JWC_SYSTEM)
                    .remove(Config.PREFERENCE_INFO_CHANNEL_SELECTED_XW)
                    .remove(Config.PREFERENCE_INFO_CHANNEL_SELECTED_TW)
                    .remove(Config.PREFERENCE_INFO_CHANNEL_SELECTED_XXB)
                    .remove(Config.PREFERENCE_HIDE_OUT_OF_DATE_EXAM)
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
