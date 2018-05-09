package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
    private static final int TOPIC_COUNT = 25;
    private final Context context;
    @Nullable
    private Document document;
    @Nullable
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

    @Nullable
    public JwTopic getJwTopic(boolean checkTemp) {
        JwTopic jwTopic = new JwTopic();
        String[] postTime = new String[TOPIC_COUNT];
        String[] postTitle = new String[TOPIC_COUNT];
        String[] postType = new String[TOPIC_COUNT];
        String[] postUrl = new String[TOPIC_COUNT];
        Elements elements = Objects.requireNonNull(document).getElementsByTag("td");

        int contentCount = 0;
        int topicCount = 0;
        boolean startText = false;
        for (Object element : elements) {
            Element element_child = (Element) element;
            if (element_child.hasText() && element_child.childNodeSize() == 1) {
                String text = element_child.text();
                if (!text.isEmpty()) {
                    if (text.contains("教务通知")) {
                        startText = true;
                        continue;
                    } else if (text.contains("教务新闻")) {
                        break;
                    }
                    if (startText) {
                        contentCount++;
                        switch (contentCount) {
                            case 1:
                                continue;
                            case 2:
                                postType[topicCount] = text.substring(1, text.indexOf("】"));
                                postTitle[topicCount] = text.substring(text.indexOf("】") + 1);
                                break;
                            case 3:
                                postTime[topicCount] = text.trim();
                                topicCount++;
                                contentCount = 0;
                                break;
                        }
                        if (topicCount >= TOPIC_COUNT) {
                            break;
                        }
                    }
                }
            }
        }

        Elements elements_url = document.getElementsByTag("a");
        List<String> attr = elements_url.eachAttr("href");
        boolean output = false;
        topicCount = 0;
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

        jwTopic.setDataVersionCode(Config.DATA_VERSION_JW_TOPIC);
        if (DataMethod.saveOfflineData(context, jwTopic, FILE_NAME, checkTemp)) {
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

    @NonNull
    public String getDetail() {
        StringBuilder result = new StringBuilder();
        Elements tags = Objects.requireNonNull(document_detail).body().getElementsByTag("p");
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

    private String loadUrl(@NonNull String url) throws IOException {
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
