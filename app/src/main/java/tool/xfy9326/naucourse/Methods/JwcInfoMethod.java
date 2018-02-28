package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.JwcTopic;

/**
 * Created by xfy9326 on 18-2-21.
 * 教务信息获取方法
 */

public class JwcInfoMethod {
    public static final String FILE_NAME = "JwcTopic";
    private static final int TOPIC_COUNT = 10;
    private final Context context;
    private JwcTopic jwcTopic;
    private Document document;
    private Document detailDocument;

    public JwcInfoMethod(Context context) {
        this.context = context;
        this.document = null;
        this.detailDocument = null;
        this.jwcTopic = null;
    }

    public boolean load() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = LoginMethod.getData(context, "/Issue/TopicList.aspx?bn=%E6%95%99%E5%8A%A1%E9%80%9A%E7%9F%A5&sn=%E6%95%99%E5%8A%A1%E9%80%9A%E7%9F%A5");
            if (data != null) {
                document = Jsoup.parse(data);
                return true;
            }
        }
        return false;
    }

    public JwcTopic getJwcTopic() {
        int divideCount = 0;
        int totalTopic = 0;
        boolean nextTopic = false;

        String type[] = new String[TOPIC_COUNT];
        String title[] = new String[TOPIC_COUNT];
        String date[] = new String[TOPIC_COUNT];
        String post[] = new String[TOPIC_COUNT];
        String click[] = new String[TOPIC_COUNT];

        jwcTopic = new JwcTopic();
        jwcTopic.setTopic_length(TOPIC_COUNT);
        Elements tags = document.body().getElementsByTag("td");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.contains("信息类别")) {
                nextTopic = true;
                continue;
            }
            if (totalTopic >= TOPIC_COUNT) {
                break;
            }
            if (nextTopic) {
                divideCount++;
                if (divideCount == 2) {
                    title[totalTopic] = str.trim();
                } else if (divideCount == 3) {
                    date[totalTopic] = str.trim();
                } else if (divideCount == 4) {
                    post[totalTopic] = str.trim();
                } else if (divideCount == 5) {
                    click[totalTopic] = str.trim();
                } else if (divideCount == 6) {
                    String temp = str.trim();
                    if (temp.contains("教务通知")) {
                        temp = context.getString(R.string.jw_system_info);
                    }
                    type[totalTopic] = temp;

                    divideCount = 0;
                    totalTopic++;
                }
            }
        }

        jwcTopic.setTopic_click(click);
        jwcTopic.setTopic_date(date);
        jwcTopic.setTopic_post(post);
        jwcTopic.setTopic_title(title);
        jwcTopic.setTopic_type(type);

        totalTopic = 0;
        String url[] = new String[TOPIC_COUNT];

        tags = document.body().getElementsByTag("a");
        data = tags.eachAttr("href");
        for (String str : data) {
            if (totalTopic >= TOPIC_COUNT) {
                break;
            }
            if (str.contains("TopicView")) {
                url[totalTopic] = "/Issue/" + str.trim();
                totalTopic++;
            }
        }

        jwcTopic.setTopic_url(url);
        BaseMethod.saveOfflineData(context, jwcTopic, FILE_NAME);
        return jwcTopic;
    }

    public boolean loadDetail(String url) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = LoginMethod.getData(context, url);
            if (data != null) {
                data = data.replace("&nbsp;", "\\b");
                detailDocument = Jsoup.parse(data);
                return true;
            }
        }
        return false;
    }

    public String getDetail() {
        StringBuilder result = new StringBuilder();
        Elements tags = detailDocument.body().getElementsByTag("p");
        Elements tags_h = detailDocument.body().getElementsByTag("h3");
        List<String> data = tags.eachText();
        List<String> data_h = tags_h.eachText();
        data.addAll(data_h);
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
