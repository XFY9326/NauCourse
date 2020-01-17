package tool.xfy9326.naucourse.methods.net;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.tools.AES;

public class SecurityMethod {
    public static void saveUserInfo(Context context, String id, String pw) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Config.PREFERENCE_USER_ID, id).apply();
        sharedPreferences.edit().putString(Config.PREFERENCE_USER_PW, AES.encrypt(pw, id)).apply();
    }

    public static String getUserId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return getUserId(sharedPreferences);
    }

    public static String getUserPassWord(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return getUserPassWord(sharedPreferences);
    }

    public static String getUserId(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
    }

    static String getUserPassWord(SharedPreferences sharedPreferences) {
        String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        String pw = sharedPreferences.getString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW);
        return pw.equalsIgnoreCase(Config.DEFAULT_PREFERENCE_USER_PW) ? Config.DEFAULT_PREFERENCE_USER_PW : AES.decrypt(pw, id);
    }

    public static String decryptData(Context context, String data) {
        if (data != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            return AES.decrypt(data, id);
        }
        return null;
    }

    public static String encryptData(Context context, String data) {
        if (data != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            return AES.encrypt(data, id);
        }
        return null;
    }
}
