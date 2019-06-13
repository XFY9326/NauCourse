package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import lib.xfy9326.net.api.API;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.tools.AES;

public class SecurityMethod {
    static final String API_KEY = "4B885B0EDE2EF94F";
    static final String API_IV = "163D738C67E431B3";
    private static final String API_TYPE = "nau_course";
    private static final String API_FUNCTION = "hiddenFunction";

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

    static String decryptData(Context context, String data) {
        if (data != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            return AES.decrypt(data, id);
        }
        return null;
    }

    static String encryptData(Context context, String data) {
        if (data != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            return AES.encrypt(data, id);
        }
        return null;
    }

    synchronized public static void unlockHiddenFunction(Context context, OnCheckHiddenFunction checkHiddenFunction) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            JSONObject json = new JSONObject();
            json.put("user_id", getUserId(sharedPreferences));
            API api = new API(API_TYPE, API_FUNCTION, API_KEY, API_IV);
            api.call(json, new API.OnRequestListener() {
                @Override
                public void OnResponse(String status, @Nullable JSONObject jsonObject) {
                    if (jsonObject == null) {
                        if (checkHiddenFunction != null) {
                            checkHiddenFunction.OnFailed();
                        }
                    } else {
                        try {
                            if (jsonObject.has("unlock_function")) {
                                boolean unlock = jsonObject.getBoolean("unlock_function");
                                sharedPreferences.edit().putBoolean(Config.PREFERENCE_SHOW_HIDDEN_FUNCTION, unlock).apply();
                                if (checkHiddenFunction != null) {
                                    checkHiddenFunction.OnSuccess(unlock);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (checkHiddenFunction != null) {
                                checkHiddenFunction.OnFailed();
                            }
                        }
                    }
                }

                @Override
                public void OnError(int errorCode, @Nullable String errorMsg) {
                    if (checkHiddenFunction != null) {
                        checkHiddenFunction.OnFailed();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (checkHiddenFunction != null) {
                checkHiddenFunction.OnFailed();
            }
        }
    }

    public interface OnCheckHiddenFunction {
        void OnSuccess(boolean canUnlock);

        void OnFailed();
    }
}
