package lib.xfy9326.naujwc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    //public static final int LOGIN_CHECKCODE_WRONG = 3;
    public static final int LOGIN_USER_INFO_WRONG = 4;
    public static final String server_url = "http://jwc.nau.edu.cn";
    private static final int LOGIN_SUCCESS = 0;
    //public static final int LOGIN_SUCCESS_SSO = -1;
    private static final String single_server_url = "http://sso.nau.edu.cn/sso/login?service=http%3a%2f%2fjwc.nau.edu.cn%2fLogin_Single.aspx";
    private static final String LOG_TAG = "NAU_JWC_CLIENT";
    @NonNull
    private final OkHttpClient client;
    @NonNull
    private final CookieStore cookieStore;
    private int loginErrorCode = LOGIN_SUCCESS;
    @Nullable
    private String loginUrl = null;

    public NauJwcClient(@NonNull Context context) {
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        cookieStore = new CookieStore(context);
        client_builder.cookieJar(cookieStore);
        client_builder.cache(getCache(context));
        client_builder.connectTimeout(8, TimeUnit.SECONDS);
        client_builder.writeTimeout(4, TimeUnit.SECONDS);
        client_builder.readTimeout(4, TimeUnit.SECONDS);
        client_builder.retryOnConnectionFailure(true);
        client = client_builder.build();
    }

    synchronized public boolean login(@NonNull String userId, @NonNull String userPw) throws Exception {
        return singleLogin(userId, userPw);
        //教务自带的登陆，由于验证码不再储存在Cookies里面所以停用
        //String loginContent = loadUrl(server_url);
        //return loginContent != null && userLogin(userId, userPw, Objects.requireNonNull(getCheckCode(loginContent)), false);
    }

    synchronized public void loginOut() throws Exception {
        userLoginOut();
    }

    //加载其他网页
    @Nullable
    public String getUserData(String requestUrl) throws Exception {
        Request.Builder builder = new Request.Builder();
        builder.url(server_url + requestUrl);
        Response response = client.newCall(builder.build()).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.string();
            } else {
                Log.d(LOG_TAG, "Get Data Response Null");
            }
        } else {
            Log.d(LOG_TAG, "Get Data Failed");
        }
        System.gc();
        return null;
    }

    @Nullable
    public String getLoginUrl() {
        return "/Students/default.aspx?" + loginUrl;
    }

    public int getLoginErrorCode() {
        return loginErrorCode;
    }

    //站点登陆
    /*private boolean userLogin(@NonNull String userId, @NonNull String userPw, @NonNull String checkcode, boolean reLogin) throws IOException {
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
                responseBody.close();
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
                    //SSO单点登陆，重新处理
                    return singleLogin(userId, userPw);
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
    }*/

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
                        //loginErrorCode = LOGIN_SUCCESS_SSO;
                        loginUrl = response.request().url().query();
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

    //登出
    private void userLoginOut() throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(server_url + "/LoginOut.aspx");
        client.newCall(builder.build()).execute().close();
        cookieStore.clearCookies();
    }

    //获取验证码
    /*@Nullable
    private String getCheckCode(@NonNull String data) throws IOException {
        String checkcode = null;
        String CheckCodeUrl = NauNetData.getCheckCodeUrl(server_url, data);
        if (CheckCodeUrl != null) {
            Request.Builder builder = new Request.Builder();
            builder.url(CheckCodeUrl);
            client.newCall(builder.build()).execute();
            checkcode = NauNetData.getCheckCode(cookieStore.getCookies(server_url).toString());
        }
        return checkcode;
    }*/

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
        return null;
    }
}
