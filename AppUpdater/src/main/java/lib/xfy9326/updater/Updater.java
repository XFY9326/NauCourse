package lib.xfy9326.updater;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import lib.xfy9326.net.api.API;

public class Updater {
    @SuppressWarnings("unused")
    public static final String UPDATE_TYPE_BETA = "beta";
    @SuppressWarnings("unused")
    public static final String UPDATE_TYPE_RELEASE = "release";
    private static final String API_TYPE = "nau_course";
    private static final String API_FUNCTION = "update";
    private static final String LOG_TAG = "APP_UPDATER";
    private final API api;

    public Updater(@NonNull String key, @NonNull String iv) {
        api = new API(API_TYPE, API_FUNCTION, key, iv);
    }

    public void checkUpdate(int versionCode, int subVersion, @NonNull String updateType, @NonNull final OnUpdateListener updateListener) {
        JSONObject updateJson = new JSONObject();
        try {
            updateJson.put("nowVersionCode", versionCode);
            updateJson.put("updateType", updateType);
            updateJson.put("subVersion", subVersion);
            updateJson.put("deviceSDK", Build.VERSION.SDK_INT);

            api.call(updateJson, new API.OnRequestListener() {
                @SuppressWarnings("unused")
                @Override
                public void OnResponse(String status, @Nullable JSONObject jsonObject) {
                    if (jsonObject == null) {
                        updateListener.onError();
                        Log.d(LOG_TAG, "DATA NO FOUND");
                    } else {
                        try {
                            if (jsonObject.getBoolean("latestVersion")) {
                                updateListener.noUpdate();
                            } else {
                                updateListener.findUpdate(jsonObject.getInt("newVersionCode"),
                                        jsonObject.getString("newVersionName"),
                                        jsonObject.getInt("subVersion"),
                                        jsonObject.getString("updateInfo"),
                                        jsonObject.getString("updateType"),
                                        jsonObject.getString("updateUrl"),
                                        jsonObject.getBoolean("forceUpdate"),
                                        jsonObject.getString("updateTime"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG, "DATA ERROR -> " + e.getMessage());
                            updateListener.onError();
                        }
                    }
                }

                @Override
                public void OnError(int errorCode, @Nullable String errorMsg) {
                    Log.d(LOG_TAG, "API ERROR -> Code:" + errorCode + " Msg:" + errorMsg);
                    updateListener.onError();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface OnUpdateListener {
        void noUpdate();

        void onError();

        void findUpdate(int versionCode, String versionName, int subVersion, String updateInfo, String updateType, String updateUrl, boolean forceUpdate, String updateTime);
    }
}
