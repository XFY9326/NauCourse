package tool.xfy9326.naucourse.methods.net;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.nausso.NauSSOClient;
import tool.xfy9326.naucourse.nausso.VPNMethod;

public class VPNMethods {
    static final String[] NON_VPN_HOST = new String[]{"jw.nau.edu.cn", "www.nau.edu.cn", "jwc.nau.edu.cn", "tw.nau.edu.cn", "xxb.nau.edu.cn", "xgc.nau.edu.cn", "my.nau.edu.cn"};
    private static final String SCHOOL_MAIN_HOST = "nau.edu.cn";

    public static void setVPNMode(Context context, boolean enabled) {
        NauSSOClient client = BaseMethod.getApp(context).getClient();
        setVPNMode(context, client, enabled);
    }

    public static void setVPNSmartMode(Context context, boolean enabled) {
        NauSSOClient client = BaseMethod.getApp(context).getClient();
        client.setVPNSmartMode(enabled, NON_VPN_HOST);
    }

    static void setVPNMode(Context context, NauSSOClient client, boolean enabled) {
        if (enabled) {
            if (!client.isVPNEnabled()) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String id = SecurityMethod.getUserId(sharedPreferences);
                String pw = SecurityMethod.getUserPassWord(sharedPreferences);
                client.enableVPNMode(id, pw);
            }
        } else {
            client.disableVPNMode();
        }
    }

    public static String vpnLinkUrlFix(Context context, String host, String url) {
        if (url.startsWith(VPNMethod.VPN_SERVER)) {
            return url;
        }
        if (url.contains(SCHOOL_MAIN_HOST) || url.startsWith("/")) {
            NauSSOClient client = BaseMethod.getApp(context).getClient();
            if (client.isVPNEnabled() && url.startsWith("/")) {
                if (!client.isVPNSmartMode() || needVPNHost(host)) {
                    if (url.contains(getRealHost(host)) || "/".equals(url)) {
                        return VPNMethod.VPN_SERVER + url;
                    } else {
                        String vpnHost = buildVPNHost(host);
                        if (vpnHost.endsWith("/") || url.startsWith("/")) {
                            return vpnHost + url;
                        } else {
                            return vpnHost + "/" + url;
                        }
                    }
                }
            }
        }
        return host + url;
    }

    private static String getRealHost(String host) {
        if (host.startsWith("http")) {
            return host.substring(7);
        } else if (host.startsWith("https")) {
            return host.substring(8);
        }
        return host;
    }

    private static String buildVPNHost(String host) {
        String realHost = getRealHost(host);
        String encryptHost = VPNMethod.encryptHost(realHost);
        if (host.startsWith("https")) {
            return VPNMethod.VPN_SERVER + "/https/" + encryptHost;
        } else {
            return VPNMethod.VPN_SERVER + "/http/" + encryptHost;
        }
    }

    private static boolean needVPNHost(String url) {
        for (String host : NON_VPN_HOST) {
            if (url.contains(host)) {
                return false;
            }
        }
        return true;
    }
}
