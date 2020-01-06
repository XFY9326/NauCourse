package lib.xfy9326.nausso;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class VPNMethod {
    static final String VPN_LOGIN_URL = "http://sso.nau.edu.cn/sso/login?service=http://vpn.nau.edu.cn/login?cas_login=true&fromUrl=/";
    private static final String VPN_HOST = "vpn.nau.edu.cn";
    public static final String VPN_SERVER = "http://" + VPN_HOST;
    private static final String VPN_URL_ENCRYPT_KEY = "wrdvpnisthebest!";
    private static final String VPN_URL_ENCRYPT_IV = "wrdvpnisthebest!";
    static String[] noVPNHost = new String[]{};

    static boolean needVPNHost(HttpUrl url) {
        for (String host : noVPNHost) {
            boolean noNeedVPN = url.host().equalsIgnoreCase(host) || url.host().equalsIgnoreCase(VPN_HOST) && url.toString().contains(host) ||
                    url.toString().contains(NauSSOClient.JWC_HOST_URL);
            if (noNeedVPN) {
                return false;
            }
        }
        return true;
    }

    static String buildVPNUrl(HttpUrl url) {
        if (url.host().equals(VPN_HOST) || url.toString().equals(VPN_LOGIN_URL)) {
            return url.toString();
        }
        StringBuilder newUrl = new StringBuilder(VPN_SERVER).append('/');
        if (url.isHttps()) {
            newUrl.append("https");
        } else {
            newUrl.append("http");
        }
        if (url.port() != 80 && url.port() != 443) {
            newUrl.append("-").append(url.port());
        }
        String urlHost = url.host();
        String encryptHost = encryptHost(urlHost);
        String oldUrl = url.toString();
        newUrl.append("/").append(encryptHost).append(oldUrl.substring(oldUrl.indexOf(urlHost) + urlHost.length()));
        return newUrl.toString();
    }

    public static String encryptHost(String host) {
        try {
            return AES.encrypt(host, VPN_URL_ENCRYPT_KEY, VPN_URL_ENCRYPT_IV);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean checkVPNLogin(String data) {
        return data != null && !(data.contains("南京审计大学统一身份认证登录") && !data.contains("vpn_hostname_data"));
    }

    static String getResponseStr(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            MediaType contentType = responseBody.contentType();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.getBuffer();
            Charset charSet = contentType != null ? contentType.charset(StandardCharsets.UTF_8) : StandardCharsets.UTF_8;
            Buffer bufferClone = buffer.clone();
            String result = bufferClone.readString(charSet != null ? charSet : StandardCharsets.UTF_8);
            bufferClone.close();
            return result;
        }
        return null;
    }
}
