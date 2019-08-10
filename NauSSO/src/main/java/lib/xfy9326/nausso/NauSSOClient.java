package lib.xfy9326.nausso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.Interceptor;
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
    private static final String ALSTU_TICKET_URL = "http://alstu.nau.edu.cn/login.aspx?cas_login=true&ticket=";
    private static final String single_login_out_url = "http://sso.nau.edu.cn/sso/logout";
    @NonNull
    private final OkHttpClient main_client;
    @NonNull
    private final OkHttpClient clean_client;
    @NonNull
    private final CookieStore cookieStore;
    private final VPNInterceptor vpnInterceptor;
    private int loginErrorCode = LOGIN_SUCCESS;
    @Nullable
    private String loginUrl = null;

    public NauSSOClient(@NonNull Context context) {
        vpnInterceptor = new VPNInterceptor();
        cookieStore = new CookieStore(context);
        main_client = buildSSOClient(context, cookieStore, vpnInterceptor);

        OkHttpClient.Builder client_clean_builder = new OkHttpClient.Builder();
        client_clean_builder.connectTimeout(8, TimeUnit.SECONDS);
        client_clean_builder.readTimeout(4, TimeUnit.SECONDS);
        client_clean_builder.writeTimeout(4, TimeUnit.SECONDS);
        clean_client = client_clean_builder.build();
    }

    /**
     * 检测用户是否登陆成功
     *
     * @param data 获取的网络数据
     * @return 是否登陆成功
     */
    public static boolean checkUserLogin(String data) {
        return data != null && !data.contains("系统错误提示页") && !data.contains("当前程序在执行过程中出现了未知异常，请重试") && !data.contains("当前你已经登录") && !data.contains("用户登录_南京审计大学教务管理系统") && !data.contains("南京审计大学统一身份认证登录");
    }

    public static boolean checkAlstuLogin(String data) {
        return data != null && !data.contains("南京审计大学统一身份认证登录") && !data.contains("location=\"LOGIN.ASPX\";");
    }

    private static OkHttpClient buildSSOClient(Context context, CookieStore cookieStore, @Nullable Interceptor interceptor) {
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        client_builder.cookieJar(cookieStore);
        client_builder.cache(getCache(context));
        client_builder.connectTimeout(15, TimeUnit.SECONDS);
        client_builder.readTimeout(8, TimeUnit.SECONDS);
        client_builder.writeTimeout(8, TimeUnit.SECONDS);
        client_builder.retryOnConnectionFailure(true);
        client_builder.connectionPool(new ConnectionPool(15, 3, TimeUnit.MINUTES));
        if (interceptor != null) {
            client_builder.addInterceptor(interceptor);
        }
        return client_builder.build();
    }

    private static Cache getCache(Context context) {
        return new Cache(context.getCacheDir(), 10240 * 1024);
    }

    private static String loadUrl(OkHttpClient client, @NonNull String url, HashMap<String, String> header) throws IOException {
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

    private static Bitmap getBitmapFromUrl(OkHttpClient client, String URL) throws Exception {
        Request request = new Request.Builder().get().url(URL).build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                InputStream inputStream = responseBody.byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                response.close();
                return bitmap;
            }
        }
        response.close();
        return null;
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

    synchronized public boolean login(@NonNull String userId, @NonNull String userPw) throws IOException {
        return singleLogin(userId, userPw);
    }

    synchronized public void loginOut() throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(single_login_out_url);
        main_client.newCall(builder.build()).execute().close();
        cookieStore.clearCookies();
    }

    /**
     * VPN注销
     *
     * @throws IOException 错误
     * @deprecated 目前VPN注销存在一直加载的问题
     */
    synchronized public void VPNLoginOut() throws IOException {
        if (isVPNEnabled()) {
            Request.Builder builder = new Request.Builder();
            builder.url(VPNMethod.VPN_SERVER + "/logout");
            main_client.newCall(builder.build()).execute().close();
        }
    }

    synchronized public void jwcLoginOut() throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(JWC_SERVER_URL + "/LoginOut.aspx");
        builder.header("Pragma", "no-cache");
        builder.header("Cache-Control", "no-cache");
        builder.header("Upgrade-Insecure-Requests", "1");
        main_client.newCall(builder.build()).execute().close();
    }

    synchronized public boolean alstuLogin(String userName, String userPw) throws IOException {
        boolean needLogin = false;
        String ssoContent = null;
        Request.Builder builder = new Request.Builder();
        builder.url(ALSTU_LOGIN_SSO_URL);
        Response response = main_client.newCall(builder.build()).execute();
        if (response.isSuccessful() && response.body() != null) {
            ssoContent = response.body().string();
            if (ssoContent.contains("南京审计大学统一身份认证登录")) {
                needLogin = true;
            }
        }
        response.close();
        if (needLogin) {
            FormBody formBody = NauNetData.getSSOPostForm(userName, userPw, ssoContent);
            Request.Builder builder2 = new Request.Builder();
            builder2.url(response.request().url());
            builder2.post(formBody);

            Response response2 = main_client.newCall(builder2.build()).execute();
            if (response2.isSuccessful()) {
                ResponseBody responseBody = response2.body();
                if (responseBody != null) {
                    String body = responseBody.string();
                    if (!body.contains("密码错误") && !body.contains("请勿输入非法字符") && !body.contains("用户名不存在")) {
                        response2.close();
                        return checkAlstuLogin(body);
                    }
                }
            }
            response2.close();
        } else {
            String ssoTicket = response.request().url().queryParameter("ticket");
            builder = new Request.Builder();
            builder.url(ALSTU_TICKET_URL + ssoTicket);
            Response responseIndex = main_client.newCall(builder.build()).execute();
            if (responseIndex.isSuccessful() && responseIndex.body() != null) {
                String data = responseIndex.body().toString();
                responseIndex.close();
                return checkAlstuLogin(data);
            }
            responseIndex.close();
        }
        return false;
    }

    @Nullable
    public String getData(String requestUrl) throws IOException {
        return loadUrl(main_client, requestUrl, null);
    }

    @Nullable
    public String loadRawUrl(String requestUrl, HashMap<String, String> header) throws IOException {
        return loadUrl(clean_client, requestUrl, header);
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
        request_builder.url(VPNMethod.VPN_LOGIN_URL);
        Response dataResponse = main_client.newCall(request_builder.build()).execute();
        if (dataResponse.isSuccessful()) {
            ResponseBody responseBody = dataResponse.body();
            if (responseBody != null) {
                ssoContent = responseBody.string();
            }
        }
        dataResponse.close();

        if (ssoContent != null && !ssoContent.contains("南京审计大学WEBVPN登录门户")) {
            FormBody formBody = NauNetData.getSSOPostForm(userName, userPw, ssoContent);
            Request.Builder builder = new Request.Builder();
            builder.url(VPNMethod.VPN_LOGIN_URL);
            builder.post(formBody);

            Response response = main_client.newCall(builder.build()).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String body = responseBody.string();
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
        String ssoContent = null;
        Request.Builder sso_request_builder = new Request.Builder();
        sso_request_builder.url(single_server_url);

        Response sso_response = main_client.newCall(sso_request_builder.build()).execute();
        if (sso_response.isSuccessful()) {
            ResponseBody responseBody = sso_response.body();
            if (responseBody != null) {
                ssoContent = responseBody.string();
            }
        }
        sso_response.close();

        if (ssoContent != null) {
            if (!ssoContent.contains("南京审计大学统一身份认证登录")) {
                loginErrorCode = LOGIN_SUCCESS;
                loginUrl = sso_response.request().url().query();
                return true;
            } else {
                FormBody formBody = NauNetData.getSSOPostForm(userId, userPw, ssoContent);
                Request.Builder builder = new Request.Builder();
                builder.url(single_server_url);
                builder.post(formBody);

                Response response = main_client.newCall(builder.build()).execute();
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String body = responseBody.string();
                        if (body.contains("密码错误") || body.contains("用户名不存在")) {
                            loginErrorCode = LOGIN_USER_INFO_WRONG;
                        } else if (body.startsWith("当前你已经登录")) {
                            loginErrorCode = LOGIN_ALREADY_LOGIN;
                        } else if (body.contains("请勿输入非法字符")) {
                            loginErrorCode = LOGIN_ERROR;
                        } else {
                            loginErrorCode = LOGIN_SUCCESS;
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
            }
        } else {
            loginErrorCode = LOGIN_ERROR;
        }
        return false;
    }

    public Bitmap getBitmap(String URL) throws Exception {
        return getBitmapFromUrl(clean_client, URL);
    }

    public Bitmap getBitmapWithLogin(String URL) throws Exception {
        return getBitmapFromUrl(main_client, URL);
    }

    public void checkServer(final OnAvailableListener availableListener) {
        Request.Builder request_builder = new Request.Builder();
        if (isVPNEnabled()) {
            request_builder.url(VPNMethod.VPN_SERVER);
        } else {
            request_builder.url(JWC_SERVER_URL);
        }
        request_builder.header("Cache-Control", "max-age=0");
        clean_client.newCall(request_builder.build()).enqueue(new Callback() {
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

    public interface OnAvailableListener {
        void OnError();
    }
}
