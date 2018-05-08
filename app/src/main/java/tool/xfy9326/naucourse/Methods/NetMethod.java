package tool.xfy9326.naucourse.Methods;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.AES;

/**
 * Created by 10696 on 2018/4/17.
 */

public class NetMethod {

    /**
     * 网络连接情况检测以及错误提示
     *
     * @param context         Context
     * @param dataLoadCode    单个数据请求错误代码
     * @param contentLoadCode 整体网络请求错误代码
     * @return 网络检查是否通过
     */
    public static boolean checkNetWorkCode(@NonNull Context context, @NonNull int[] dataLoadCode, int contentLoadCode) {
        if (contentLoadCode == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) {
            Toast.makeText(context, R.string.network_get_error, Toast.LENGTH_SHORT).show();
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
                    return networkInfo.isAvailable();
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
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                }
            }
        }
        return false;
    }

    /**
     * Ping IP测试
     *
     * @param ipAddress IP地址
     * @return 是否能Ping通
     */
    public static boolean pingIpAddress(String ipAddress) {
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 2 " + ipAddress);
            int status = process.waitFor();
            return status == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查i-NAU-Home网络SSID
     *
     * @param context Context
     * @return 是否是i-NAU-Home网络
     */
    public static boolean checkNauWifiSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            String ssid = wifiManager.getConnectionInfo().getSSID().trim();
            return ssid.equals("\"i-NAU-Home\"");
        }
        return false;
    }

    /**
     * 获取本地IP地址
     *
     * @param context Context
     * @return IP地址
     */
    public static String getLocalIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            return intToIp(wifiManager.getConnectionInfo().getIpAddress()).trim();
        }
        return null;
    }

    /**
     * int转IP地址
     *
     * @param i IP数值
     * @return IP地址
     */
    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 直接登陆i-NAU-Home
     * 无UI和提示，存在问题直接看日志 WIFI_CONNECT
     *
     * @param context Context
     */
    public static void loginNAUWifi(@NonNull final Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (NetMethod.isNetworkConnected(context) && NetMethod.isWifiNetWork(context)) {
                if (NetMethod.checkNauWifiSSID(context)) {
                    if (NetMethod.pingIpAddress("172.26.3.20")) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String id = sharedPreferences.getString(Config.PREFERENCE_NETWORK_ACCOUNT, Config.DEFAULT_PREFERENCE_USER_ID);
                        String pw = sharedPreferences.getString(Config.PREFERENCE_NETWORK_PASSWORD, Config.DEFAULT_PREFERENCE_USER_PW);
                        if (!id.equals(Config.DEFAULT_PREFERENCE_USER_ID) && !pw.equals(Config.DEFAULT_PREFERENCE_USER_PW)) {
                            pw = AES.decrypt(pw, id);
                            int provider = sharedPreferences.getInt(Config.PREFERENCE_NETWORK_PROVIDER, Config.DEFAULT_PREFERENCE_NETWORK_PROVIDER);
                            WifiLoginMethod wifiLoginMethod = new WifiLoginMethod(NetMethod.getLocalIP(context));
                            wifiLoginMethod.setOnRequestListener(new WifiLoginMethod.OnRequestListener() {
                                @Override
                                public void OnRequest(boolean isSuccess, int errorType) {
                                    if (!isSuccess) {
                                        if (errorType == WifiLoginMethod.ERROR_TYPE_LOGIN_ERROR) {
                                            Log.d("WIFI_CONNECT", "Login Error");
                                        } else if (errorType == WifiLoginMethod.ERROR_TYPE_REQUEST_ERROR) {
                                            Log.d("WIFI_CONNECT", "Request Error");
                                        }
                                    } else {
                                        Log.d("WIFI_CONNECT", "Login Success");
                                        NotificationMethod.showWifiConnectSuccess(context);
                                    }
                                }
                            });
                            String type = null;
                            switch (provider) {
                                case 0:
                                    type = WifiLoginMethod.TYPE_TELECOM;
                                    break;
                                case 1:
                                    type = WifiLoginMethod.TYPE_CMCC;
                                    break;
                                case 2:
                                    type = WifiLoginMethod.TYPE_UNICOM;
                                    break;
                                case 3:
                                    type = WifiLoginMethod.TYPE_SCHOOL;
                                    break;
                            }
                            if (type != null) {
                                wifiLoginMethod.login(id, Objects.requireNonNull(pw), type);
                            } else {
                                Log.d("WIFI_CONNECT", "Type Error");
                            }
                        } else {
                            Log.d("WIFI_CONNECT", "Info Error");
                        }
                    } else {
                        Log.d("WIFI_CONNECT", "Server Error");
                    }
                } else {
                    Log.d("WIFI_CONNECT", "SSID Error");
                }
            } else {
                Log.d("WIFI_CONNECT", "NetWork Error");
            }
        } else {
            Log.d("WIFI_CONNECT", "Permission Error");
        }
    }

}
