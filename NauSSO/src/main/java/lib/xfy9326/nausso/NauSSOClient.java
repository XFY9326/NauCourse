package lib.xfy9326.nausso;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class NauSSOClient {
    public static final int LOGIN_ERROR = 1;
    public static final int LOGIN_ALREADY_LOGIN = 2;
    public static final int LOGIN_USER_INFO_WRONG = 4;
    public static final String JWC_SERVER_URL = "http://jwc.nau.edu.cn";
    static final String JWC_HOST_URL = "jwc.nau.edu.cn";
    private static final int LOGIN_SUCCESS = 0;
    private static final String single_server_url = "http://sso.nau.edu.cn/sso/login?service=http%3a%2f%2fjwc.nau.edu.cn%2fLogin_Single.aspx";
    private static final String ALSTU_LOGIN_SSO_URL = "http://sso.nau.edu.cn/sso/login?service=http%3a%2f%2falstu.nau.edu.cn%2flogin.aspx";
    private static final String ALSTU_TICKET_URL = "http://alstu.nau.edu.cn/login.aspx?ticket=";
    private static final String single_login_out_url = "http://sso.nau.edu.cn/sso/logout";
    private static boolean needLogoutBeforeReLogin = false;
    @NonNull
    private final OkHttpClient client;
    private final OkHttpClient client_clean;
    @NonNull
    private final CookieStore cookieStore;
    private final VPNInterceptor vpnInterceptor;
    private int loginErrorCode = LOGIN_SUCCESS;
    @Nullable
    private String loginUrl = null;

    public NauSSOClient(@NonNull Context context) {
        vpnInterceptor = new VPNInterceptor();
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        cookieStore = new CookieStore(context);
        client_builder.cookieJar(cookieStore);
        client_builder.cache(getCache(context));
        client_builder.connectTimeout(15, TimeUnit.SECONDS);
        client_builder.readTimeout(5, TimeUnit.SECONDS);
        client_builder.writeTimeout(5, TimeUnit.SECONDS);
        client_builder.retryOnConnectionFailure(true);
        client_builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
        client_builder.addInterceptor(vpnInterceptor);
        client = client_builder.build();

        OkHttpClient.Builder client_clean_builder = new OkHttpClient.Builder();
        client_clean_builder.connectTimeout(8, TimeUnit.SECONDS);
        client_clean_builder.readTimeout(4, TimeUnit.SECONDS);
        client_clean_builder.writeTimeout(4, TimeUnit.SECONDS);
        client_clean = client_clean_builder.build();
    }

    public static boolean isNeedLogoutBeforeReLogin() {
        return needLogoutBeforeReLogin;
    }

    /**
     * 检测用用户是否登陆成功
     *
     * @param data 获取的网络数据
     * @return 是否登陆成功
     */
    public static boolean checkUserLogin(String data) {
        if (data != null) {
            if (data.contains("系统错误提示页") || data.contains("当前程序在执行过程中出现了未知异常，请重试") || data.contains("当前你已经登录") || data.contains("用户登录_南京审计大学教务管理系统")) {
                needLogoutBeforeReLogin = true;
                return false;
            } else
                return !data.contains("南京审计大学统一身份认证登录") && !data.contains("location=\"LOGIN.ASPX\";");
        }
        return false;
    }

    public boolean isVPNEnabled() {
        return vpnInterceptor.isEnabled();
    }

    public boolean isVPNSmartMode() {
        if (isVPNEnabled()) {
            return vpnInterceptor.isSmartMode();
        }
        return false;
    }

    public void setVPNSmartMode(boolean enabled, String[] noVPNHost) {
        if (enabled) {
            VPNInterceptor.setNoVPNHost(noVPNHost);
        }
        vpnInterceptor.setSmartMode(enabled);
    }

    public void enableVPNMode(String vpnUserName, String vpnUserPw) {
        vpnInterceptor.setVPNUser(vpnUserName, vpnUserPw);
        vpnInterceptor.setEnabled(true);
    }

    public void disableVPNMode() {
        vpnInterceptor.setEnabled(false);
    }

    synchronized public boolean login(@NonNull String userId, @NonNull String userPw) throws Exception {
        return singleLogin(userId, userPw);
    }

    synchronized public void loginOut() throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url(single_login_out_url);
        client.newCall(builder.build()).execute().close();
        cookieStore.clearCookies();
        needLogoutBeforeReLogin = false;
    }

    /**
     * VPN注销
     *
     * @throws Exception 错误
     * @deprecated 目前VPN注销存在一直加载的问题
     */
    synchronized public void VPNLoginOut() throws Exception {
        if (isVPNEnabled()) {
            Request.Builder builder = new Request.Builder();
            builder.url(VPNInterceptor.VPN_SERVER + "/logout");
            client.newCall(builder.build()).execute().close();
        }
    }

    synchronized public void jwcLoginOut() throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url(JWC_SERVER_URL + "/LoginOut.aspx");
        client.newCall(builder.build()).execute().close();
        needLogoutBeforeReLogin = false;
    }

    synchronized public void alstuLogin() throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url(ALSTU_LOGIN_SSO_URL);
        Response response = client.newCall(builder.build()).execute();

        String ssoTicket = response.request().url().queryParameter("ticket");
        response.close();

        builder = new Request.Builder();
        builder.url(ALSTU_TICKET_URL + ssoTicket);
        client.newCall(builder.build()).execute().close();
    }

    @Nullable
    public String getData(String requestUrl) throws Exception {
        return loadUrl(requestUrl);
    }

    @Nullable
    public String getJwcLoginUrl() {
        return "/Students/default.aspx?" + loginUrl;
    }

    public int getLoginErrorCode() {
        return loginErrorCode;
    }

    private void VPNLogin(@NonNull String userName, @NonNull String userPw) throws IOException {
        String ssoContent = null;

        Request.Builder request_builder = new Request.Builder();
        request_builder.url(VPNInterceptor.VPN_LOGIN_URL);
        Response dataResponse = client.newCall(request_builder.build()).execute();
        if (dataResponse.isSuccessful()) {
            ResponseBody responseBody = dataResponse.body();
            if (responseBody != null) {
                ssoContent = responseBody.string();
            }
            dataResponse.close();
        }
        if (ssoContent != null && !ssoContent.contains("南京审计大学WEBVPN登录门户")) {
            FormBody formBody = NauNetData.getSSOPostForm(userName, userPw, ssoContent);
            Request.Builder builder = new Request.Builder();
            builder.url(VPNInterceptor.VPN_LOGIN_URL);
            builder.post(formBody);

            Response response = client.newCall(builder.build()).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String body = responseBody.string();
                    responseBody.close();
                    if (!body.contains("密码错误") && !body.contains("请勿输入非法字符") && body.contains("南京审计大学WEBVPN登录门户")) {
                        response.close();
                    }
                }
                response.close();
            }
        }
    }

    //SSO单点登录
    private boolean singleLogin(@NonNull String userId, @NonNull String userPw) throws IOException {
        if (isVPNEnabled()) {
            VPNLogin(userId, userPw);
        }
        //缓存SSO的Cookies
        String ssoContent = loadUrl(single_server_url);
        if (ssoContent != null) {
            FormBody formBody = NauNetData.getSSOPostForm(userId, userPw, ssoContent);
            Request.Builder builder = new Request.Builder();
            builder.url(single_server_url);
            builder.post(formBody);

            Response response = client.newCall(builder.build()).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String body = responseBody.string();
                    if (body.contains("密码错误")) {
                        loginErrorCode = LOGIN_USER_INFO_WRONG;
                    } else if (body.startsWith("当前你已经登录")) {
                        loginErrorCode = LOGIN_ALREADY_LOGIN;
                        needLogoutBeforeReLogin = true;
                    } else if (body.contains("请勿输入非法字符")) {
                        loginErrorCode = LOGIN_ERROR;
                    } else {
                        loginErrorCode = LOGIN_SUCCESS;
                        needLogoutBeforeReLogin = false;
                        loginUrl = response.request().url().query();
                        response.close();
                        return true;
                    }
                } else {
                    loginErrorCode = LOGIN_ERROR;
                }
                response.close();
            } else {
                loginErrorCode = LOGIN_ERROR;
            }
        } else {
            loginErrorCode = LOGIN_ERROR;
        }
        return false;
    }

    public void checkServer(final OnAvailableListener availableListener) {
        Request.Builder request_builder = new Request.Builder();
        if (isVPNEnabled()) {
            request_builder.url(VPNInterceptor.VPN_SERVER);
        } else {
            request_builder.url(JWC_SERVER_URL);
        }
        request_builder.header("Cache-Control", "max-age=0");
        client_clean.newCall(request_builder.build()).enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException e) {
                availableListener.OnError();
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) {
                if (response.code() != 200) {
                    availableListener.OnError();
                }
                response.close();
            }
        });
    }

    private Cache getCache(Context context) {
        return new Cache(context.getCacheDir(), 10240 * 1024);
    }

    private String loadUrl(String url) throws IOException {
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

    public interface OnAvailableListener {
        void OnError();
    }
}
