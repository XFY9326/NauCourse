package tool.xfy9326.naucourse.methods.net;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.nausso.NauSSOClient;

/**
 * Created by xfy9326 on 18-2-20.
 * 登陆登出以及获取数据方法
 */

public class LoginMethod {
    static final ReentrantLock RE_LOGIN_LOCK = new ReentrantLock();

    /**
     * 用户Cookie过期后尝试重新登陆
     *
     * @param context Context
     * @return ReLogin状态值
     * @throws IOException 重新登陆时的网络错误
     */
    static int reLogin(@NonNull Context context) throws IOException, InterruptedException {
        if (RE_LOGIN_LOCK.tryLock()) {
            try {
                return doReLogin(context);
            } finally {
                RE_LOGIN_LOCK.unlock();
            }
        } else {
            return Config.RE_LOGIN_TRYING;
        }
    }

    public static int reLogin(@NonNull Context context, String id, String pw, SharedPreferences sharedPreferences) throws IOException, InterruptedException {
        if (RE_LOGIN_LOCK.tryLock()) {
            try {
                return doReLogin(context, id, pw, sharedPreferences);
            } finally {
                RE_LOGIN_LOCK.unlock();
            }
        } else {
            return Config.RE_LOGIN_TRYING;
        }
    }

    private static int doReLogin(@NonNull Context context) throws IOException, InterruptedException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = SecurityMethod.getUserId(sharedPreferences);
        String pw = SecurityMethod.getUserPassWord(sharedPreferences);
        if (!pw.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_PW) && !id.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_ID)) {
            return doReLogin(context, id, pw, sharedPreferences);
        }
        return Config.RE_LOGIN_FAILED;
    }

    private static int doReLogin(@NonNull Context context, String id, String pw, SharedPreferences sharedPreferences) throws IOException, InterruptedException {
        NauSSOClient nauSSOClient = BaseMethod.getApp(context).getClient();
        nauSSOClient.jwcLoginOut();
        Thread.sleep(2000);
        if (nauSSOClient.login(id, pw)) {
            nauSSOClient.alstuLogin(id, pw);
            String loginURL = nauSSOClient.getJwcLoginUrl();
            if (loginURL != null) {
                sharedPreferences.edit().putString(Config.PREFERENCE_LOGIN_URL, loginURL).apply();
                return Config.RE_LOGIN_SUCCESS;
            }
        }
        return Config.RE_LOGIN_FAILED;
    }

    synchronized static boolean doAlstuReLogin(@NonNull Context context) throws IOException {
        NauSSOClient nauSSOClient = BaseMethod.getApp(context).getClient();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = SecurityMethod.getUserId(sharedPreferences);
        String pw = SecurityMethod.getUserPassWord(sharedPreferences);
        if (!pw.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_PW) && !id.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_ID)) {
            return nauSSOClient.alstuLogin(id, pw);
        }
        return false;
    }

    /**
     * 注销用户登陆
     *
     * @param context Context
     * @return 是否成功注销登陆
     */
    public static boolean loginOut(@NonNull Context context) {
        try {
            NauSSOClient nauSSOClient = BaseMethod.getApp(context).getClient();
            nauSSOClient.jwcLoginOut();
            nauSSOClient.VPNLoginOut();
            nauSSOClient.loginOut();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().remove(Config.PREFERENCE_LOGIN_URL)
                    .remove(Config.PREFERENCE_LOGIN_URL)
                    .remove(Config.PREFERENCE_NETWORK_ACCOUNT)
                    .remove(Config.PREFERENCE_NETWORK_PASSWORD)
                    .remove(Config.PREFERENCE_NETWORK_REMEMBER_PASSWORD)
                    .remove(Config.PREFERENCE_PERSONAL_INFO_LOAD_DATE_TIME)
                    .remove(Config.PREFERENCE_COURSE_TABLE_AUTO_LOAD_DATE_TIME)
                    .remove(Config.PREFERENCE_UPDATE_DATA_ON_START)
                    .remove(Config.PREFERENCE_ONLY_UPDATE_UNDER_WIFI)
                    .remove(Config.PREFERENCE_ONLY_UPDATE_APPLICATION_UNDER_WIFI)
                    .remove(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND)
                    .remove(Config.PREFERENCE_SCHOOL_CALENDAR_URL)
                    .remove(Config.PREFERENCE_CLASS_BEFORE_NOTIFY)
                    .remove(Config.PREFERENCE_CUSTOM_TERM_START_DATE)
                    .remove(Config.PREFERENCE_SCHOOL_CALENDAR_NAME)
                    .remove(Config.PREFERENCE_SCHOOL_CALENDAR_PAGE_URL)
                    .remove(Config.PREFERENCE_SCHOOL_CALENDAR_URL)
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
                    .remove(Config.PREFERENCE_SCHOOL_VPN_SMART_MODE)
                    .remove(Config.PREFERENCE_EULA_ACCEPT)
                    .remove(Config.PREFERENCE_SHOW_HIDDEN_FUNCTION)
                    .putBoolean(Config.PREFERENCE_HAS_LOGIN, false)
                    .apply();
            TempMethod.cleanUserTemp(context);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
