package tool.xfy9326.naucourse.Methods;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 10696 on 2018/4/17.
 */

public class WifiLoginMethod {
    public static final String TYPE_TELECOM = "telecom";
    public static final String TYPE_CMCC = "cmcc";
    public static final String TYPE_UNICOM = "unicom";
    public static final String TYPE_SCHOOL = "school";

    public static final int ERROR_TYPE_SUCCESS = 0;
    public static final int ERROR_TYPE_REQUEST_ERROR = 1;
    public static final int ERROR_TYPE_LOGIN_ERROR = 2;

    private final OkHttpClient client;
    private final String ClientIP;
    private OnRequestListener onRequestListener = null;

    /**
     * 构造
     *
     * @param clientIP 客户端IP地址
     */
    public WifiLoginMethod(String clientIP) {
        this.ClientIP = clientIP;
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        client_builder.connectTimeout(10, TimeUnit.SECONDS);
        client_builder.writeTimeout(8, TimeUnit.SECONDS);
        client_builder.readTimeout(8, TimeUnit.SECONDS);
        client = client_builder.build();
    }

    /**
     * 构造登陆的数据表格
     *
     * @param id   账户ID
     * @param pw   账户密码
     * @param type 网络提供商类型
     * @return 表格
     */
    private static FormBody buildLoginForm(String id, String pw, String type) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("DDDDD", ",0," + id.trim() + "@" + type);
        builder.add("upass", pw.trim());
        builder.add("R1", "0");
        builder.add("R2", "");
        builder.add("R3", "");
        builder.add("R6", "0");
        builder.add("para", "00");
        builder.add("0MKKey", "123456");
        return builder.build();
    }

    /**
     * 构造注销的数据表格
     *
     * @param id 账户ID
     * @param pw 账户密码
     * @return 表格
     */
    private static FormBody buildLoginOutForm(String id, String pw) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("DDDDD", id.trim());
        builder.add("upass", pw.trim());
        return builder.build();
    }

    /**
     * 构造请求的网络地址
     *
     * @param ip      客户端IP地址
     * @param isLogin 是否是登陆时的网络地址
     * @return 网络地址
     */
    private static String buildURL(String ip, boolean isLogin) {
        String urlType;
        if (isLogin) {
            urlType = "Login";
        } else {
            urlType = "Logout";
        }
        return "http://172.26.3.20:801/eportal/" +
                "?c=ACSetting" +
                "&a=" + urlType +
                "&wlanuserip=" + ip +
                "&wlanacip=" +
                "&wlanacname=" +
                "&redirect=" +
                "&session=" +
                "&vlanid=0" +
                "&ssid=" +
                "&port=" +
                "&iTermType=1" +
                "&protocol=http:" +
                "&queryACIP=0";
    }

    /**
     * 登录
     *
     * @param id   登录的ID
     * @param pw   登录的密码
     * @param type 登录的网络服务提供商类型
     */
    public void login(String id, String pw, String type) {
        requestURL(buildURL(ClientIP, true), buildLoginForm(id, pw, type));
    }

    /**
     * 注销
     *
     * @param id 账户ID
     * @param pw 账户密码
     */
    public void loginOut(String id, String pw) {
        requestURL(buildURL(ClientIP, false), buildLoginOutForm(id, pw));
    }

    /**
     * 网络请求
     *
     * @param url      网络请求的地址
     * @param formBody 网络请求的数据表格
     */
    private void requestURL(String url, FormBody formBody) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(formBody);
        //第一次请求
        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onRequestListener != null) {
                    onRequestListener.OnRequest(false, ERROR_TYPE_REQUEST_ERROR);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response != null && response.request() != null) {
                    String url = response.request().url().toString();
                    //登录错误判断
                    if (url.contains("ErrorMsg")) {
                        if (onRequestListener != null) {
                            onRequestListener.OnRequest(false, ERROR_TYPE_LOGIN_ERROR);
                        }
                    } else {
                        //第二次请求
                        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                if (onRequestListener != null) {
                                    onRequestListener.OnRequest(false, ERROR_TYPE_REQUEST_ERROR);
                                }
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                if (onRequestListener != null) {
                                    onRequestListener.OnRequest(true, ERROR_TYPE_SUCCESS);
                                }
                            }
                        });
                    }
                    response.close();
                }
            }
        });
    }

    /**
     * 设置请求监听
     *
     * @param onRequestListener 请求监听
     */
    public void setOnRequestListener(OnRequestListener onRequestListener) {
        this.onRequestListener = onRequestListener;
    }

    public interface OnRequestListener {
        void OnRequest(boolean isSuccess, int errorType);
    }
}
