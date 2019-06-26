package lib.xfy9326.nausso;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSource;

public class VPNInterceptor implements Interceptor {
    static final String VPN_LOGIN_URL = "http://sso.nau.edu.cn/sso/login?service=http://vpn.nau.edu.cn/login?cas_login=true&fromUrl=/";
    private static final String VPN_HOST = "vpn.nau.edu.cn";
    public static final String VPN_SERVER = "http://" + VPN_HOST;
    private static final String VPN_COOKIES = "http://vpn.nau.edu.cn/wengine-vpn/";
    private static String[] noVPNHost = new String[]{};
    private boolean enabled = false;
    private boolean smartMode = false;
    private String userName;
    private String userPw;

    static void setNoVPNHost(String[] hostList) {
        noVPNHost = hostList;
    }

    private static boolean checkVPNLogin(String data) {
        return data != null && !(data.contains("南京审计大学统一身份认证登录") && !data.contains("vpn_hostname_data"));
    }

    private static String getResponseStr(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            MediaType contentType = responseBody.contentType();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = Util.bomAwareCharset(source, contentType != null ? contentType.charset(StandardCharsets.UTF_8) : StandardCharsets.UTF_8);
            Buffer bufferClone = buffer.clone();
            String result = bufferClone.readString(charset);
            bufferClone.close();
            return result;
        }
        return null;
    }

    private static boolean needVPNHost(HttpUrl url) {
        for (String host : noVPNHost) {
            if (url.host().equalsIgnoreCase(host) || url.host().equalsIgnoreCase(VPN_HOST) && url.toString().contains(host) ||
                    url.toString().contains(NauSSOClient.JWC_HOST_URL)) {
                return false;
            }
        }
        return true;
    }

    private static String buildVPNUrl(HttpUrl url) {
        if (url.host().equals(VPN_HOST) || url.toString().equals(VPN_LOGIN_URL)) {
            return url.toString();
        }
        StringBuilder newUrl = new StringBuilder(VPN_SERVER).append('/');
        String simpleUrl = url.toString();
        if (url.isHttps()) {
            newUrl.append("https");
            simpleUrl = simpleUrl.substring(8);
        } else {
            newUrl.append("http");
            simpleUrl = simpleUrl.substring(7);
        }
        if (url.port() != 80 && url.port() != 443) {
            newUrl.append("-").append(url.port());
        }
        newUrl.append("/").append(simpleUrl);
        return newUrl.toString();
    }

    boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && (userName == null || userPw == null)) {
            this.enabled = false;
        }
    }

    boolean isSmartMode() {
        return smartMode;
    }

    void setSmartMode(boolean enabled) {
        this.smartMode = enabled;
    }

    void setVPNUser(String userName, String userPw) {
        this.userName = userName;
        this.userPw = userPw;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        if (enabled && !url.toString().equals(VPN_LOGIN_URL) && !url.toString().startsWith(VPN_COOKIES)) {
            if (smartMode) {
                if (needVPNHost(url)) {
                    return VPNLoad(chain, request, url, true);
                }
            } else {
                return VPNLoad(chain, request, url, true);
            }
        }
        return chain.proceed(request);
    }

    private Response VPNLoad(Chain chain, Request request, HttpUrl url, boolean tryReLogin) throws IOException {
        String newUrl = buildVPNUrl(url);
        Request newRequest = request.newBuilder().url(newUrl).build();

        Response response = chain.proceed(newRequest);
        if (response.isSuccessful()) {
            String body = getResponseStr(response);
            if (body != null) {
                if (checkVPNLogin(body)) {
                    if (body.contains("南京审计大学WEBVPN登录门户")) {
                        response.close();
                        return chain.proceed(newRequest);
                    } else {
                        return response;
                    }
                } else if (tryReLogin) {
                    if (VPNLogin(chain)) {
                        response.close();
                        return VPNLoad(chain, request, url, false);
                    } else {
                        return chain.proceed(newRequest);
                    }
                }
            }
        }
        return response;
    }

    synchronized private boolean VPNLogin(Chain chain) throws IOException {
        String ssoContent = null;

        Request.Builder request_builder = new Request.Builder();
        request_builder.url(VPN_LOGIN_URL);
        Response dataResponse = chain.proceed(request_builder.build());
        if (dataResponse.isSuccessful()) {
            ResponseBody responseBody = dataResponse.body();
            if (responseBody != null) {
                ssoContent = responseBody.string();
            }
        }
        dataResponse.close();

        if (ssoContent != null) {
            if (ssoContent.contains("南京审计大学WEBVPN登录门户")) {
                return true;
            }
            FormBody formBody = NauNetData.getSSOPostForm(userName, userPw, ssoContent);
            Request.Builder builder = new Request.Builder();
            builder.url(VPN_LOGIN_URL);
            builder.post(formBody);

            Response response = chain.proceed(builder.build());
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String body = responseBody.string();
                    if (!body.contains("密码错误") && !body.contains("请勿输入非法字符") && body.contains("南京审计大学WEBVPN登录门户")) {
                        response.close();
                        return true;
                    }
                }
            }
            response.close();
        }
        return false;
    }
}
