package lib.xfy9326.naujwc;

import android.content.Context;
import android.util.Log;

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

public class NauJwcClient {
    public static final int LOGIN_ERROR = 1;
    public static final int LOGIN_ALREADY_LOGIN = 2;
    public static final int LOGIN_CHECKCODE_WRONG = 3;
    public static final int LOGIN_USER_INFO_WRONG = 4;
    public static final String server_url = "http://jwc.nau.edu.cn";
    private static final int LOGIN_SUCCESS = 0;
    private final OkHttpClient client;
    private final CookieStore cookieStore;
    private int loginErrorCode = LOGIN_SUCCESS;
    private String loginUrl = null;

    public NauJwcClient(Context context) {
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        cookieStore = new CookieStore(context);
        client_builder.cookieJar(cookieStore);
        client_builder.cache(getCache(context));
        client_builder.connectTimeout(8, TimeUnit.SECONDS);
        client_builder.writeTimeout(8, TimeUnit.SECONDS);
        client_builder.readTimeout(8, TimeUnit.SECONDS);
        client_builder.retryOnConnectionFailure(true);
        client = client_builder.build();
    }

    synchronized public boolean login(String userId, String userPw) throws Exception {
        String url = loadUrl();
        return url != null && userLogin(userId, userPw, getCheckCode(url), false);
    }

    synchronized public void loginOut() throws Exception {
        userLoginOut();
    }

    //加载其他网页
    public String getUserData(String requestUrl) throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url(server_url + requestUrl);
        Response response = client.newCall(builder.build()).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.string();
            } else {
                Log.d("NAU_JWC_CLIENT", "Get Data Response Null");
            }
        } else {
            Log.d("NAU_JWC_CLIENT", "Get Data Failed");
        }
        System.gc();
        return null;
    }

    public String getLoginUrl() {
        return "/Students/default.aspx?" + loginUrl;
    }

    public int getLoginErrorCode() {
        return loginErrorCode;
    }

    //登陆
    private boolean userLogin(String userId, String userPw, String checkcode, boolean reLogin) throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(server_url + "/Login.aspx");

        FormBody.Builder form_builder = new FormBody.Builder();
        form_builder.add("UserName", userId);
        form_builder.add("UserPwd", userPw);
        form_builder.add("checkCode", checkcode);
        form_builder.add("btnLogin", "登录");

        builder.post(form_builder.build());

        Response response = client.newCall(builder.build()).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String body = responseBody.string();
                if (body.startsWith("当前你已经登录")) {
                    loginErrorCode = LOGIN_ALREADY_LOGIN;
                    return false;
                } else if (body.contains("验证码错误")) {
                    loginErrorCode = LOGIN_CHECKCODE_WRONG;
                    if (!reLogin) {
                        userLoginOut();
                        return userLogin(userId, userPw, checkcode, true);
                    }
                } else if (body.contains("请勿输入非法字符")) {
                    loginErrorCode = LOGIN_ERROR;
                } else if (body.contains("my.nau.edu.cn/bmportal/index.portal")) {
                    loginErrorCode = LOGIN_USER_INFO_WRONG;
                } else {
                    loginErrorCode = LOGIN_SUCCESS;
                    loginUrl = response.request().url().query();
                    return true;
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

    //登出
    private void userLoginOut() throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(server_url + "/LoginOut.aspx");
        client.newCall(builder.build()).execute();
        cookieStore.clearCookies();
    }

    //获取验证码
    private String getCheckCode(String data) throws IOException {
        String checkcode = null;
        String CheckCodeUrl = NauNetData.getCheckCodeUrl(server_url, data);
        if (CheckCodeUrl != null) {
            Request.Builder builder = new Request.Builder();
            builder.url(CheckCodeUrl);
            client.newCall(builder.build()).execute();
            checkcode = NauNetData.getCheckCode(cookieStore.getCookies(server_url).toString());
        }
        return checkcode;
    }

    private String loadUrl() throws IOException {
        Request.Builder request_builder = new Request.Builder();
        request_builder.url(server_url);

        Response response = client.newCall(request_builder.build()).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.string();
            }
        }
        return null;
    }
}
