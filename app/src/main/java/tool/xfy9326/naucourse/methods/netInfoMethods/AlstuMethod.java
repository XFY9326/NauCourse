package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.VPNMethods;
import tool.xfy9326.naucourse.utils.AlstuTopic;

public class AlstuMethod extends BaseInfoDetailMethod<AlstuTopic> {
    public static final String FILE_NAME = AlstuTopic.class.getSimpleName();
    public static final int TYPE_ALSTU = 5;
    public static final String ALSTU_SERVER_URL = "http://alstu.nau.edu.cn/";
    private static final String ALSTU_MSG_URL = ALSTU_SERVER_URL + "MESSAGE/DEFAULT.ASPX";
    private Document detailDocument = null;
    private Document document = null;

    public AlstuMethod(@NonNull Context context) {
        super(context);
    }

    private static String getDownloadFileText(Context context, Document detailDocument) {
        StringBuilder result = new StringBuilder();
        Element elementData = Objects.requireNonNull(detailDocument).body().getElementById("MyDataList");
        if (elementData != null) {
            Elements elementsTd = elementData.getElementsByTag("td");

            result.append("<br/><br/><br/><p>附件：</p>");

            Element elementTjsj = Objects.requireNonNull(detailDocument).body().getElementById("tjsj");
            String year = elementTjsj.text().substring(0, 4);

            for (Element elementChild : elementsTd) {
                Element a = elementChild.getElementsByTag("a").first();
                if (a.hasAttr("onclick")) {
                    String fileDownloadName = a.attr("onclick");
                    fileDownloadName = fileDownloadName.substring(fileDownloadName.indexOf("\'") + 1, fileDownloadName.lastIndexOf("\'"));
                    result.append(combineDownloadText(context, year, fileDownloadName, a.text()));
                }

            }
        }
        return result.toString();
    }

    private static String combineDownloadText(Context context, String year, String fileDownloadName, String fileName) {
        return "<p><a href=\"" + VPNMethods.vpnLinkUrlFix(context, ALSTU_SERVER_URL, "/aldfdnf.aspx?lx=st&ylx=" + year + "&file=" + fileDownloadName) + "\">" + fileName + "</a></p>";
    }

    @Override
    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, ALSTU_MSG_URL, true);
            if (data != null) {
                if (NauSSOClient.checkAlstuLogin(data)) {
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
    @Override
    public AlstuTopic getData(boolean checkTemp) {
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
                        Elements elementsA = elementChild.getElementsByTag("a");
                        String href = elementsA.get(0).attr("href");
                        if (href.startsWith("../")) {
                            href = href.substring(3);
                        }
                        url.add(href);
                        break;
                    case 2:
                        date.add(text);
                        counter = 0;
                        break;
                    default:
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

    @Override
    public int loadDetail(String url) throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, VPNMethods.vpnLinkUrlFix(context, ALSTU_SERVER_URL, url), true);
            if (data != null) {
                if (NauSSOClient.checkUserLogin(data)) {
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
    @Override
    public String getDetailData() {
        Element elementBody = Objects.requireNonNull(detailDocument).body();
        Elements elementsP = elementBody.getElementById("nr").getElementsByTag("p");
        elementsP.remove(0);
        String result = elementsP.html().replaceAll("<img.*?/?>", context.getResources().getString(R.string.image_replace));
        result += getDownloadFileText(context, detailDocument);
        return result;
    }
}
