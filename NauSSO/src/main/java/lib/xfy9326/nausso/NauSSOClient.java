package lib.xfy9326.nausso;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class NauSSOClient {
    public static final int LOGIN_ERROR = 1;
    public static final int LOGIN_ALREADY_LOGIN = 2;
    public static final int LOGIN_USER_INFO_WRONG = 4;
    public static final String JWC_SERVER_URL = "http://jwc.nau.edu.cn";
    private static final int LOGIN_SUCCESS = 0;
    private static final String single_server_url = "http://sso.nau.edu.cn/sso/login?service=http%3a%2f%2fjwc.nau.edu.cn%2fLogin_Single.aspx";
    private static final String ALSTU_LOGIN_SSO_URL = "http://sso.nau.edu.cn/sso/login?service=http%3a%2f%2falstu.nau.edu.cn%2flogin.aspx";
    private static final String ALSTU_TICKET_URL = "http://alstu.nau.edu.cn/login.aspx?ticket=";
    private static final String single_login_out_url = "http://sso.nau.edu.cn/sso/logout";
    private static final String LOG_TAG = "NAU_SSO_CLIENT";
    @NonNull
    private final OkHttpClient client;
    @NonNull
    private final CookieStore cookieStore;
    private int loginErrorCode = LOGIN_SUCCESS;
    @Nullable
    private String loginUrl = null;

    public NauSSOClient(@NonNull Context context) {
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        cookieStore = new CookieStore(context);
        client_builder.cookieJar(cookieStore);
        client_builder.cache(getCache(context));
        client_builder.connectTimeout(10, TimeUnit.SECONDS);
        client_builder.writeTimeout(8, TimeUnit.SECONDS);
        client_builder.readTimeout(8, TimeUnit.SECONDS);
        client_builder.retryOnConnectionFailure(true);
        client = client_builder.build();
    }

    synchronized public boolean login(@NonNull String userId, @NonNull String userPw) throws Exception {
        return singleLogin(userId, userPw);
    }

    synchronized public void loginOut() throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url(single_login_out_url);
        client.newCall(builder.build()).execute().close();
        cookieStore.clearCookies();
    }

    synchronized public void jwcLoginOut() throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url(JWC_SERVER_URL + "/LoginOut.aspx");
        client.newCall(builder.build()).execute().close();
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
        Request.Builder builder = new Request.Builder();
        builder.url(requestUrl);
        Response response = client.newCall(builder.build()).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String data = responseBody.string();
                response.close();
                return data;
            } else {
                Log.d(LOG_TAG, "Get Data Response Null");
            }
        } else {
            Log.d(LOG_TAG, "Get Data Failed");
        }
        response.close();
        return null;
    }

    @Nullable
    public String getJwcLoginUrl() {
        return "/Students/default.aspx?" + loginUrl;
    }

    public int getLoginErrorCode() {
        return loginErrorCode;
    }

    //SSO单点登录
    private boolean singleLogin(@NonNull String userId, @NonNull String userPw) throws IOException {
        //清空旧的Cookies
        cookieStore.clearCookies();
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
                    responseBody.close();
                    if (body.contains("密码错误")) {
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
            } else {
                loginErrorCode = LOGIN_ERROR;
            }
        } else {
            loginErrorCode = LOGIN_ERROR;
        }
        return false;
    }

    private Cache getCache(Context context) {
        return new Cache(context.getCacheDir(), 10240 * 1024);
    }

    @SuppressWarnings("SameParameterValue")
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
}
