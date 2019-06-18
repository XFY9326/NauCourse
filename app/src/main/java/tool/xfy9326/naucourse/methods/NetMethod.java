package tool.xfy9326.naucourse.methods;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import lib.xfy9326.nausso.NauSSOClient;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;

/**
 * Created by 10696 on 2018/4/17.
 */

public class NetMethod {
    public static boolean showConnectErrorOnce = false;
    private static OkHttpClient okHttpClient = null;
    private static boolean showLoginErrorOnce = false;

    public static NauSSOClient getNewSSOClient(Context context) {
        NauSSOClient client = new NauSSOClient(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        client.setVPNSmartMode(sharedPreferences.getBoolean(Config.PREFERENCE_SCHOOL_VPN_SMART_MODE, Config.DEFAULT_PREFERENCE_SCHOOL_VPN_SMART_MODE), VPNMethods.NON_VPN_HOST);
        VPNMethods.setVPNMode(context, client, sharedPreferences.getBoolean(Config.PREFERENCE_SCHOOL_VPN_MODE, Config.DEFAULT_PREFERENCE_SCHOOL_VPN_MODE));
        return client;
    }

    /**
     * 加载一个网页
     *
     * @param url 网页URL
     * @return 页面内容
     * @throws IOException 网页获取错误
     */
    public static String loadUrl(@NonNull String url) throws IOException {
        if (okHttpClient == null) {
            OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
            client_builder.connectTimeout(8, TimeUnit.SECONDS);
            client_builder.readTimeout(4, TimeUnit.SECONDS);
            client_builder.writeTimeout(4, TimeUnit.SECONDS);
            okHttpClient = client_builder.build();
        }
        return loadUrl(okHttpClient, url, null);
    }

    public static String loadUrl(OkHttpClient client, @NonNull String url, HashMap<String, String> header) throws IOException {
        Request.Builder request_builder = new Request.Builder();
        request_builder.url(url);
        if (header != null) {
            for (String key : header.keySet()) {
                String value = header.get(key);
                request_builder.header(key, value == null ? "" : value);
            }
        }
        Response response = client.newCall(request_builder.build()).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String result = responseBody.string();
                response.close();
                return result;
            }
        }
        response.close();
        return null;
    }

    /**
     * POST一个网页
     *
     * @param client Client
     * @param url    网页URL
     * @param form   POST Form
     * @param header Header
     * @return 页面内容
     * @throws IOException 网页获取错误
     */
    public static String postUrl(@NonNull OkHttpClient client, @NonNull String url, @NonNull HashMap<String, String> form, @NonNull HashMap<String, String> header) throws IOException {
        FormBody.Builder form_builder = new FormBody.Builder();
        for (String key : form.keySet()) {
            String value = form.get(key);
            form_builder.add(key, value == null ? "" : value);
        }
        Request.Builder request_builder = new Request.Builder();
        request_builder.url(url);
        request_builder.post(form_builder.build());
        for (String key : header.keySet()) {
            String value = header.get(key);
            request_builder.header(key, value == null ? "" : value);
        }
        Response response = client.newCall(request_builder.build()).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String result = responseBody.string();
                response.close();
                return result;
            }
        }
        response.close();
        return null;
    }

    /**
     * 通过登陆的客户端获取数据
     * 必须在非UI线程运行
     *
     * @param context    Context
     * @param url        需要获取的url
     * @param tryReLogin 检测到登陆错误后是否尝试重新登陆
     * @return 获取的数据字符串
     * @throws Exception 网络连接中的错误
     */
    public static String loadUrlFromLoginClient(@NonNull Context context, String url, boolean tryReLogin) throws Exception {
        String data = BaseMethod.getApp(context).getClient().getData(url);
        boolean userLogin = NauSSOClient.checkUserLogin(data);
        boolean alstuLogin = NauSSOClient.checkAlstuLogin(data);
        if (!(userLogin && alstuLogin) && tryReLogin) {
            if (userLogin) {
                if (LoginMethod.doAlstuReLogin(context)) {
                    Thread.sleep(500);
                    return BaseMethod.getApp(context).getClient().getData(url);
                } else {
                    return null;
                }
            }
            int reLogin_result = LoginMethod.reLogin(context);
            switch (reLogin_result) {
                case Config.RE_LOGIN_SUCCESS:
                    data = BaseMethod.getApp(context).getClient().getData(url);
                    break;
                case Config.RE_LOGIN_TRYING:
                    while (LoginMethod.isTryingReLogin) {
                        Thread.sleep(500);
                    }
                    Thread.sleep(500);
                    return loadUrlFromLoginClient(context, url, false);
                case Config.RE_LOGIN_FAILED:
                    Thread.sleep(500);
                    reLogin_result = LoginMethod.reLogin(context);
                    if (reLogin_result == Config.RE_LOGIN_SUCCESS) {
                        return loadUrlFromLoginClient(context, url, false);
                    }
                    break;
            }
        }
        return data;
    }

    /**
     * 网络连接情况检测以及错误提示
     *
     * @param context                  Context
     * @param dataLoadCode             单个数据请求错误代码
     * @param contentLoadCode          整体网络请求错误代码
     * @param ignoreSingleGetDataError 忽略多个加载结果中单个网络数据获取失败
     * @return 网络检查是否通过
     */
    synchronized public static boolean checkNetWorkCode(@NonNull Context context, @NonNull int[] dataLoadCode, int contentLoadCode, boolean ignoreSingleGetDataError) {
        if (contentLoadCode == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) {
            if (!showConnectErrorOnce) {
                Toast.makeText(context, R.string.network_get_error, Toast.LENGTH_SHORT).show();
                showConnectErrorOnce = true;
            }
            return false;
        }
        int getDataErrorCount = 0;
        for (int code : dataLoadCode) {
            if (code == Config.NET_WORK_ERROR_CODE_TIME_OUT) {
                Toast.makeText(context, R.string.network_get_error, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN) {
                if (!showLoginErrorOnce) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    showLoginErrorOnce = true;
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA) {
                if (!showLoginErrorOnce) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    showLoginErrorOnce = true;
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR) {
                if (dataLoadCode.length > 1 && ignoreSingleGetDataError && getDataErrorCount + 1 < dataLoadCode.length) {
                    getDataErrorCount++;
                } else {
                    Toast.makeText(context, R.string.data_get_error, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 网络是否联接检测
     *
     * @param context Context
     * @return 网络是否联接
     */
    public static boolean isNetworkConnected(@Nullable Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    return networkInfo.isConnected();
                }
            }
        }
        return false;
    }

    /**
     * WIFI网络检测
     *
     * @param context Context
     * @return WIFI网络是否联接
     */
    public static boolean isWifiNetWork(@Nullable Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                    if (networkCapabilities != null) {
                        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                    }
                } else {
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null) {
                        return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 通过浏览器打开Url的网页
     *
     * @param context Context
     * @param url     地址
     */
    public static void viewUrlInBrowser(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.launch_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 教务处网络联通判断
     *
     * @param activity Activity
     */
    public static void checkJwcAvailable(Activity activity) {
        NauSSOClient client = BaseMethod.getApp(activity).getClient();
        if (client != null) {
            client.checkServer(() -> activity.runOnUiThread(() -> Toast.makeText(activity, R.string.school_net_no_connection, Toast.LENGTH_SHORT).show()));
        }
    }
}
