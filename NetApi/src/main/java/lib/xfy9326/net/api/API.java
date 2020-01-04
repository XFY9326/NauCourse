package lib.xfy9326.net.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class API {
    @SuppressWarnings("WeakerAccess")
    public static final int ERROR_TYPE_REQUEST_AES_ERROR = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int ERROR_TYPE_REQUEST_FAILED = 2;
    @SuppressWarnings("WeakerAccess")
    public static final int ERROR_TYPE_RESPONSE_DATA_ERROR = 3;
    @SuppressWarnings("WeakerAccess")
    public static final int ERROR_TYPE_RESPONSE_AES_ERROR = 4;
    @SuppressWarnings("WeakerAccess")
    public static final String STATUS_SUCCESS = "SUCCESS";
    @SuppressWarnings("unused")
    public static final String STATUS_FAILED = "FAILED";
    @SuppressWarnings("unused")
    public static final String STATUS_SQL_DATA_CONNECT_ERROR = "SQL_DATA_CONNECT_ERROR";
    @SuppressWarnings("unused")
    public static final String STATUS_SQL_SECURITY_CONNECT_ERROR = "SQL_SECURITY_CONNECT_ERROR";
    @SuppressWarnings("unused")
    public static final String STATUS_REQUEST_FORMAT_ERROR = "REQUEST_FORMAT_ERROR";
    private static final String SERVER_URL = "https://www.xfy9326.top/api/";
    private static String API_KEY;
    private static String API_IV;
    private static String API_TYPE;
    private static String API_FUNCTION;
    @NonNull
    private final OkHttpClient client;

    public API(@NonNull String type, @NonNull String function, @NonNull String key, @NonNull String iv) {
        API_KEY = key;
        API_IV = iv;
        API_TYPE = type;
        API_FUNCTION = function;

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(20, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(10, TimeUnit.SECONDS);
        clientBuilder.readTimeout(10, TimeUnit.SECONDS);
        client = clientBuilder.build();
    }

    public void call(@NonNull JSONObject data, @NonNull final OnRequestListener requestListener) {
        String dataStr = data.toString();
        try {
            dataStr = AES.encrypt(dataStr, API_KEY, API_IV);

            final JSONObject requestJson = new JSONObject();
            requestJson.put("data", dataStr);
            requestJson.put("time", System.currentTimeMillis() / 1000);
            requestJson.put("api_key", API_KEY);

            final RequestBody requestBody = FormBody.create(requestJson.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request.Builder builder = new Request.Builder();
            builder.url(SERVER_URL + "/" + API_TYPE + "/" + API_FUNCTION + ".php");
            builder.post(requestBody);

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requestListener.onError(ERROR_TYPE_REQUEST_FAILED, e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String body = responseBody.string();
                        responseBody.close();
                        response.close();
                        try {
                            JSONObject responseJson = new JSONObject(body);
                            responseJson.getString("status");
                            if (responseJson.getString("status").equals(STATUS_SUCCESS)) {
                                responseJson.getString("data");
                                responseJson.getString("api_key");
                                if (responseJson.getString("api_key").equals(API_KEY)) {
                                    String resultStr = responseJson.getString("data");
                                    try {
                                        resultStr = AES.decrypt(resultStr, API_KEY, API_IV);
                                        try {
                                            JSONObject result = new JSONObject(resultStr);
                                            requestListener.onResponse(STATUS_SUCCESS, result);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            requestListener.onError(ERROR_TYPE_RESPONSE_DATA_ERROR, e.getMessage());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        requestListener.onError(ERROR_TYPE_RESPONSE_AES_ERROR, e.getMessage());
                                    }
                                }
                            } else {
                                requestListener.onResponse(requestJson.getString("status"), null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            requestListener.onError(ERROR_TYPE_RESPONSE_DATA_ERROR, e.getMessage());
                        }
                    } else {
                        requestListener.onError(ERROR_TYPE_RESPONSE_DATA_ERROR, null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            requestListener.onError(ERROR_TYPE_REQUEST_AES_ERROR, e.getMessage());
        }

    }

    public interface OnRequestListener {
        @SuppressWarnings("unused")
        void onResponse(String status, @Nullable JSONObject jsonObject);

        void onError(int errorCode, @Nullable String errorMsg);
    }
}
