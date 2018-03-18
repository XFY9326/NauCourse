package tool.xfy9326.naucourse.Methods;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.JwTopic;

/**
 * Created by 10696 on 2018/2/28.
 * 获取教务信息
 */

public class JwInfoMethod {
    public static final String FILE_NAME = "JwTopic";
    public static final String server_url = "http://jw.nau.edu.cn";
    private static final int TOPIC_COUNT = 20;
    private final Context context;
    private Document document;
    private Document document_detail;

    public JwInfoMethod(Context context) {
        this.context = context;
        this.document = null;
        this.document_detail = null;
    }

    public int load() throws Exception {
        String data = loadUrl(server_url);
        System.gc();
        if (data != null) {
            document = Jsoup.parse(data);
            if (LoginMethod.checkUserLogin(data)) {
                document = Jsoup.parse(data);
                return Config.NET_WORK_GET_SUCCESS;
            }
            return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public JwTopic getJwTopic(boolean checkTemp) {
        JwTopic jwTopic = new JwTopic();
        String[] postTime = new String[TOPIC_COUNT];
        String[] postTitle = new String[TOPIC_COUNT];
        String[] postType = new String[TOPIC_COUNT];
        String[] postUrl = new String[TOPIC_COUNT];
        Elements elements = document.getElementsByTag("tr");
        List<String> text = elements.eachText();
        for (String str : text) {
            if (str.contains("教务通知")) {
                str = str.substring(5);
                str = str.substring(0, str.indexOf("教务新闻")).trim();
                String[] data = str.split(" ");
                int topicCount = 0;
                int contentCount = 0;
                for (String txt : data) {
                    if (topicCount >= TOPIC_COUNT) {
                        break;
                    }
                    if (contentCount == 0) {
                        postType[topicCount] = txt.substring(1, txt.indexOf("】"));
                        postTitle[topicCount] = txt.substring(txt.indexOf("】") + 1);
                        contentCount++;
                    } else if (contentCount == 1) {
                        postTime[topicCount] = txt;
                        contentCount = 0;
                        topicCount++;
                    }
                }
                break;
            }
        }

        Elements elements_url = document.getElementsByTag("a");
        List<String> attr = elements_url.eachAttr("href");
        boolean output = false;
        int topicCount = 0;
        for (String str : attr) {
            if (topicCount >= TOPIC_COUNT) {
                break;
            }
            if (str.contains("/s/185/t/404/p/11/list.htm")) {
                output = true;
                continue;
            }
            if (str.contains("/s/185/t/404/p/12/list.htm")) {
                output = false;
            }
            if (output) {
                postUrl[topicCount] = str.trim();
                topicCount++;
            }
        }

        jwTopic.setPostType(postType);
        jwTopic.setPostTitle(postTitle);
        jwTopic.setPostTime(postTime);
        jwTopic.setPostUrl(postUrl);
        jwTopic.setPostLength(TOPIC_COUNT);

        if (BaseMethod.saveOfflineData(context, jwTopic, FILE_NAME, checkTemp)) {
            return jwTopic;
        } else {
            return null;
        }
    }

    public int loadDetail(String url) throws Exception {
        String data = loadUrl(server_url + url);
        if (data != null) {
            data = data.replace("&nbsp;", "\\b");
            if (LoginMethod.checkUserLogin(data)) {
                document_detail = Jsoup.parse(data);
                return Config.NET_WORK_GET_SUCCESS;
            }
            return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public String getDetail() {
        StringBuilder result = new StringBuilder();
        Elements tags = document_detail.body().getElementsByTag("p");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.equals("") || str.equals(" ")) {
                result.append("\n");
            } else {
                result.append(str.replace("\\b", " ")).append("\n");
            }
        }
        return result.toString();
    }

    public String[] getExtraFileUrl() {
        ArrayList<String> list = new ArrayList<>();
        Elements tags = document_detail.body().getElementsByAttributeValueContaining("href", "/picture/article/");
        List<String> data = tags.eachAttr("href");
        for (String str : data) {
            list.add(server_url + str);
        }
        return list.toArray(new String[]{});
    }

    private String loadUrl(String url) throws IOException {
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        OkHttpClient client = client_builder.build();
        Request.Builder request_builder = new Request.Builder();
        request_builder.url(url);
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
