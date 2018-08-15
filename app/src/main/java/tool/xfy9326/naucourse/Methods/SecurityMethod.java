package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Tools.AES;

public class SecurityMethod {
    public static final String API_KEY = "4B885B0EDE2EF94F";
    public static final String API_IV = "163D738C67E431B3";

    public static void saveUserInfo(Context context, String id, String pw) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Config.PREFERENCE_USER_ID, id).apply();
        sharedPreferences.edit().putString(Config.PREFERENCE_USER_PW, AES.encrypt(pw, id)).apply();
    }

    public static String getUserPassWord(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        String pw = sharedPreferences.getString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW);
        return pw.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_PW) ? Config.DEFAULT_PREFERENCE_USER_PW : AES.decrypt(pw, id);
    }

    public static String decryptData(Context context, String data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        return BuildConfig.DEBUG ? data : AES.decrypt(data, id);
    }

    public static String encryptData(Context context, String data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        return BuildConfig.DEBUG ? data : AES.encrypt(data, id);
    }
}
