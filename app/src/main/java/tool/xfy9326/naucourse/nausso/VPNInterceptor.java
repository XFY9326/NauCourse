package tool.xfy9326.naucourse.nausso;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

class VPNInterceptor implements Interceptor {
    private static final String VPN_COOKIES = "http://vpn.nau.edu.cn/wengine-vpn/";
    private boolean enabled = false;
    private boolean smartMode = false;
    private String userName;
    private String userPw;

    static void setNoVPNHost(String[] hostList) {
        VPNMethod.noVPNHost = hostList;
    }

    boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        boolean canNotEnable = enabled && (userName == null || userPw == null);
        if (canNotEnable) {
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

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        if (enabled && !url.toString().equals(VPNMethod.VPN_LOGIN_URL) && !url.toString().startsWith(VPN_COOKIES)) {
            if (smartMode) {
                if (VPNMethod.needVPNHost(url)) {
                    return loadVPN(chain, request, url, true);
                }
            } else {
                return loadVPN(chain, request, url, true);
            }
        }
        return chain.proceed(request);
    }

    private Response loadVPN(Chain chain, Request request, HttpUrl url, boolean tryReLogin) throws IOException {
        String newUrl = VPNMethod.buildVPNUrl(url);
        Request newRequest = request.newBuilder().url(newUrl).build();

        Response response = chain.proceed(newRequest);
        if (response.isSuccessful()) {
            String body = VPNMethod.getResponseStr(response);
            if (body != null) {
                if (VPNMethod.checkVPNLogin(body)) {
                    if (body.contains("南京审计大学WEBVPN登录门户")) {
                        response.close();
                        return chain.proceed(newRequest);
                    } else {
                        return response;
                    }
                } else if (tryReLogin) {
                    if (loginVPN(chain)) {
                        response.close();
                        return loadVPN(chain, request, url, false);
                    } else {
                        return chain.proceed(newRequest);
                    }
                }
            }
        }
        return response;
    }

    synchronized private boolean loginVPN(Chain chain) throws IOException {
        String ssoContent = null;

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(VPNMethod.VPN_LOGIN_URL);
        Response dataResponse = chain.proceed(requestBuilder.build());
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
            builder.url(VPNMethod.VPN_LOGIN_URL);
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
