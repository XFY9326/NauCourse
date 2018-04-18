package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Created by 10696 on 2018/4/17.
 */

public class NetMethod {

    /**
     * 网络是否联接检测
     *
     * @param context Context
     * @return 网络是否联接
     */
    public static boolean isNetworkConnected(Context context) {
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
    public static boolean isWifiNetWork(Context context) {
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
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 3 " + ipAddress);
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

}
