package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.JwTopic;

/**
 * Created by 10696 on 2018/2/28.
 * 获取教务信息
 */

public class JwInfoMethod {
    public static final String FILE_NAME = "JwTopic";
    public static final String server_url = "http://jw.nau.edu.cn";
    private static final String calendar_server_utl = "http://www.nau.edu.cn";
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
        String data = NetMethod.loadUrl(server_url);
        System.gc();
        if (data != null) {
            document = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
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

    public int loadSchoolCalendarImage(boolean checkTemp) throws Exception {
        String Image_Url = null;
        if (document != null) {
            String url = null;
            Elements elements = document.getElementsByTag("a");
            for (Element element : elements) {
                if (element.hasText() && element.text().contains("校历") && element.hasAttr("href")) {
                    url = element.attr("href");
                    break;
                }
            }
            if (url != null) {
                String data = NetMethod.loadUrl(url);
                if (data != null) {
                    Document document = Jsoup.parse(data);
                    Elements elements_img = document.getElementsByClass("readinfo");
                    for (Element element : elements_img) {
                        Elements elements_img_2 = element.getElementsByTag("img");
                        for (Element element_2 : elements_img_2) {
                            if (element_2.hasAttr("src")) {
                                Image_Url = calendar_server_utl + element_2.attr("src");
                            }
                        }
                    }
                }
            }
        }
        if (Image_Url != null) {
            if (checkTemp) {
                String old_url = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_SCHOOL_CALENDAR_URL, null);
                if (old_url != null && old_url.equalsIgnoreCase(Image_Url)) {
                    return Config.NET_WORK_GET_SUCCESS;
                }
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Config.PREFERENCE_SCHOOL_CALENDAR_URL, Image_Url).apply();
            if (ImageMethod.downloadImage(Image_Url, ImageMethod.getSchoolCalendarImagePath(context))) {
                return Config.NET_WORK_GET_SUCCESS;
            }
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public Bitmap getSchoolCalendarImage() {
        return ImageMethod.getSchoolCalendarImage(context);
    }

    public int loadDetail(String url) throws Exception {
        String data = NetMethod.loadUrl(server_url + url);
        if (data != null) {
            data = data.replace("&nbsp;", "\\b");
            document_detail = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
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
}
