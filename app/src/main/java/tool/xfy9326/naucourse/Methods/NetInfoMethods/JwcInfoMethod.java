package tool.xfy9326.naucourse.Methods.NetInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.JwcTopic;

/**
 * Created by xfy9326 on 18-2-21.
 * 获取教务系统信息
 */

public class JwcInfoMethod {
    public static final String FILE_NAME = "JwcTopic";
    public static final int TYPE_JWC = 0;
    private static final int TOPIC_COUNT = 15;
    private final Context context;
    @Nullable
    private JwcTopic jwcTopic;
    @Nullable
    private Document document;
    @Nullable
    private Document detailDocument;

    public JwcInfoMethod(Context context) {
        this.context = context;
        this.document = null;
        this.detailDocument = null;
        this.jwcTopic = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Issue/TopicList.aspx?bn=%E6%95%99%E5%8A%A1%E9%80%9A%E7%9F%A5&sn=%E6%95%99%E5%8A%A1%E9%80%9A%E7%9F%A5", true);
            if (data != null) {
                if (LoginMethod.checkUserLogin(data)) {
                    document = Jsoup.parse(data);
                    return Config.NET_WORK_GET_SUCCESS;
                }
                return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
            }
            return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
        }
        return Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN;
    }

    @Nullable
    public JwcTopic getJwcTopic() {
        int divideCount = 0;
        int totalTopic = 0;
        boolean nextTopic = false;

        String[] type = new String[TOPIC_COUNT];
        String[] title = new String[TOPIC_COUNT];
        String[] date = new String[TOPIC_COUNT];
        String[] post = new String[TOPIC_COUNT];
        String[] click = new String[TOPIC_COUNT];

        jwcTopic = new JwcTopic();
        jwcTopic.setTopic_length(TOPIC_COUNT);
        Elements tags = Objects.requireNonNull(document).body().getElementsByTag("td");
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
                switch (divideCount) {
                    case 2:
                        title[totalTopic] = str.trim();
                        break;
                    case 3:
                        date[totalTopic] = str.trim();
                        break;
                    case 4:
                        post[totalTopic] = str.trim();
                        break;
                    case 5:
                        click[totalTopic] = str.trim();
                        break;
                    case 6:
                        String temp = str.trim();
                        if (temp.contains("教务通知")) {
                            temp = context.getString(R.string.jw_system_info);
                        }
                        type[totalTopic] = temp;

                        divideCount = 0;
                        totalTopic++;
                        break;
                }
            }
        }

        jwcTopic.setTopic_click(click);
        jwcTopic.setTopic_date(date);
        jwcTopic.setTopic_post(post);
        jwcTopic.setTopic_title(title);
        jwcTopic.setTopic_type(type);

        totalTopic = 0;
        String[] url = new String[TOPIC_COUNT];

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
        jwcTopic.setDataVersionCode(Config.DATA_VERSION_JWC_TOPIC);
        return jwcTopic;
    }

    public int loadDetail(String url) throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + url, true);
            if (data != null) {
                if (LoginMethod.checkUserLogin(data)) {
                    detailDocument = Jsoup.parse(data);
                    return Config.NET_WORK_GET_SUCCESS;
                }
                return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
            }
            return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
        }
        return Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN;
    }

    @NonNull
    public String getDetail() {
        StringBuilder result = new StringBuilder();
        if (detailDocument != null) {
            Elements tags = detailDocument.body().getElementsByTag("tr");
            String[] temp = tags.html().split("</td>");
            boolean nextContent = false;
            for (String str : temp) {
                if (str.contains("发布者：")) {
                    nextContent = true;
                    continue;
                }
                if (nextContent) {
                    result.append(str);
                }
            }
        }
        return result.toString().replaceAll("<img.*?/?>", context.getResources().getString(R.string.image_replace));
    }
}
