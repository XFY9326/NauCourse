package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
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

    /**
     * 加载一个网页
     *
     * @param url 网页URL
     * @return 页面内容
     * @throws IOException 网页获取错误
     */
    public static String loadUrl(@NonNull String url) throws IOException {
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        OkHttpClient client = client_builder.build();
        Request.Builder request_builder = new Request.Builder();
        request_builder.url(url);
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
     * 网络连接情况检测以及错误提示
     *
     * @param context         Context
     * @param dataLoadCode    单个数据请求错误代码
     * @param contentLoadCode 整体网络请求错误代码
     * @return 网络检查是否通过
     */
    synchronized public static boolean checkNetWorkCode(@NonNull Context context, @NonNull int[] dataLoadCode, int contentLoadCode) {
        if (contentLoadCode == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) {
            if (!BaseMethod.getApp(context).isShowConnectErrorOnce()) {
                Toast.makeText(context, R.string.network_get_error, Toast.LENGTH_SHORT).show();
                BaseMethod.getApp(context).setShowConnectErrorOnce(true);
            }
            return false;
        }
        for (int code : dataLoadCode) {
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN) {
                if (!BaseMethod.getApp(context).isShowLoginErrorOnce()) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    BaseMethod.getApp(context).setShowLoginErrorOnce();
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA) {
                if (!BaseMethod.getApp(context).isShowLoginErrorOnce()) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    BaseMethod.getApp(context).setShowLoginErrorOnce();
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR) {
                Toast.makeText(context, R.string.data_get_error, Toast.LENGTH_SHORT).show();
                return false;
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
                        //noinspection deprecation
                        return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 教务处网络联通判断
     *
     * @param availableListener 联通接口
     */
    public static void isJwcAvailable(final OnAvailableListener availableListener) {
        Request.Builder request_builder = new Request.Builder();
        request_builder.url("http://jwc.nau.edu.cn");
        new OkHttpClient().newCall(request_builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                availableListener.OnError();
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() != 200) {
                    availableListener.OnError();
                }
            }
        });
    }

    public interface OnAvailableListener {
        void OnError();
    }
}
