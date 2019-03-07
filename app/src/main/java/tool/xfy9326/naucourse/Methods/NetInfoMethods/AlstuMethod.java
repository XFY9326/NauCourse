package tool.xfy9326.naucourse.Methods.NetInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.AlstuTopic;

public class AlstuMethod {
    public static final String FILE_NAME = "AlstuTopic";
    public static final int TYPE_ALSTU = 5;
    public static final String ALSTU_SERVER_URL = "http://alstu.nau.edu.cn/";
    private static final String ALSTU_MSG_URL = ALSTU_SERVER_URL + "MESSAGE/DEFAULT.ASPX";
    private final Context context;
    private Document document;
    private Document detailDocument;

    public AlstuMethod(@NonNull Context context) {
        this.context = context;
        this.document = null;
        this.detailDocument = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, ALSTU_MSG_URL, true);
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

    public AlstuTopic getAlstuTopic() {
        AlstuTopic alstuTopic = new AlstuTopic();
        ArrayList<String> title = new ArrayList<>();
        ArrayList<String> url = new ArrayList<>();
        ArrayList<String> date = new ArrayList<>();

        Element element = Objects.requireNonNull(document).body().getElementById("MyDataGrid");
        if (element != null) {
            Elements elements = element.getElementsByTag("td");

            int counter = 0;
            for (Element elementChild : elements) {
                //跳出页码选择
                if (elementChild.hasAttr("colspan") && "2".equals(elementChild.attr("colspan"))) {
                    break;
                }
                String text = elementChild.text().replace("\n", "").trim();
                counter++;
                switch (counter) {
                    case 1:
                        title.add(text);
                        Elements elements_a = elementChild.getElementsByTag("a");
                        String href = elements_a.get(0).attr("href");
                        if (href.startsWith("../")) {
                            href = href.substring(3);
                        }
                        url.add(href);
                        break;
                    case 2:
                        date.add(text);
                        counter = 0;
                        break;
                }
            }
            alstuTopic.setTopic_length(title.size());
            alstuTopic.setTopicTitle(title.toArray(new String[]{}));
            alstuTopic.setTopicUrl(url.toArray(new String[]{}));
            alstuTopic.setTopicDate(date.toArray(new String[]{}));
            alstuTopic.setDataVersionCode(Config.DATA_VERSION_ALSTU_TOPIC);
        }
        return alstuTopic;
    }

    public int loadDetail(String url) throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, ALSTU_SERVER_URL + url, true);
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

    private static String getDownloadFileText(Document detailDocument) {
        StringBuilder result = new StringBuilder();
        Element element_data = Objects.requireNonNull(detailDocument).body().getElementById("MyDataList");
        if (element_data != null) {
            Elements elements_td = element_data.getElementsByTag("td");

            result.append("<br/><br/><br/><p>附件：</p>");

            Element element_tjsj = Objects.requireNonNull(detailDocument).body().getElementById("tjsj");
            String year = element_tjsj.text().substring(0, 4);

            for (Element elementChild : elements_td) {
                Element a = elementChild.getElementsByTag("a").first();
                if (a.hasAttr("onclick")) {
                    String fileDownloadName = a.attr("onclick");
                    fileDownloadName = fileDownloadName.substring(fileDownloadName.indexOf("\'") + 1, fileDownloadName.lastIndexOf("\'"));
                    result.append(combineDownloadText(year, fileDownloadName, a.text()));
                }

            }
        }
        return result.toString();
    }

    private static String combineDownloadText(String year, String fileDownloadName, String fileName) {
        return "<p><a href=\"" + ALSTU_SERVER_URL + "aldfdnf.aspx?lx=st&ylx=" + year + "&file=" + fileDownloadName + "\">" + fileName + "</a></p>";
    }

    @NonNull
    public String getDetail() {
        Element element = Objects.requireNonNull(detailDocument).body().getElementById("nr");
        Elements elements_p = element.getElementsByTag("p");
        elements_p.remove(0);
        String result = elements_p.html().replaceAll("<img.*?/?>", "");
        result += getDownloadFileText(detailDocument);
        return result;
    }
}
