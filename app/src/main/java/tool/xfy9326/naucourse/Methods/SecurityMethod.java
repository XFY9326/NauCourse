package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Tools.AES;

public class SecurityMethod {
    public static void saveUserInfo(Context context, String id, String pw) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Config.PREFERENCE_USER_PW, AES.encrypt(pw, id)).apply();
    }

    public static String getUserPassWord(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        String pw = sharedPreferences.getString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW);
        return AES.decrypt(pw, id);
    }
}