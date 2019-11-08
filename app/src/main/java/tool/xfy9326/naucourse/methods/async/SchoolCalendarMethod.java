package tool.xfy9326.naucourse.methods.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;

/**
 * Created by 10696 on 2018/2/28.
 * 获取教务信息
 */

public class SchoolCalendarMethod extends BaseNetMethod {
    private static final String CALENDAR_URL_CURRENT = "http://nau.edu.cn/5825/list.htm";
    private static final String CALENDAR_SERVER_URL = "https://www.nau.edu.cn";
    private static final String CALENDAR_LIST = "https://www.nau.edu.cn/p141c89/list.htm";
    private final SharedPreferences sharedPreferences;
    @Nullable
    private Document documentList;

    public SchoolCalendarMethod(@NonNull Context context) {
        super(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int load() throws Exception {
        String data = NetMethod.loadUrl(context, CALENDAR_LIST);
        if (data != null) {
            documentList = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    @Nullable
    public LinkedHashMap<String, String> getCalendarUrlList() {
        LinkedHashMap<String, String> calendarList = new LinkedHashMap<>();
        if (documentList != null) {
            Elements elements = documentList.select("span[class=cols_title]");
            for (Element element : elements) {
                Elements aArr = element.getElementsByTag("a");
                if (aArr != null && aArr.size() != 0) {
                    Element a = aArr.first();
                    String name = a.text().trim();
                    String url = a.attr("href").trim();
                    calendarList.put(name, CALENDAR_SERVER_URL + url);
                }
            }
        }
        return calendarList;
    }

    public int loadSchoolCalendarImage(boolean checkTemp) throws Exception {
        String url = sharedPreferences.getString(Config.PREFERENCE_SCHOOL_CALENDAR_PAGE_URL, CALENDAR_URL_CURRENT);
        return saveImageUrl(url, checkTemp);
    }

    private int saveImageUrl(String imagePageUrl, boolean checkTemp) throws Exception {
        String imageUrl = null;
        if (imagePageUrl != null) {
            String data = NetMethod.loadUrl(context, imagePageUrl);
            if (data != null) {
                Document document = Jsoup.parse(data);
                Elements elementsImg = document.select("div[class=wp_articlecontent]");
                Element element = elementsImg.get(0).getElementsByTag("img").first();
                if (element.hasAttr("src")) {
                    imageUrl = CALENDAR_SERVER_URL + element.attr("src");
                }
            }
        }
        if (imageUrl != null) {
            if (checkTemp) {
                String oldUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_SCHOOL_CALENDAR_URL, null);
                if (oldUrl != null && oldUrl.equalsIgnoreCase(imageUrl)) {
                    return Config.NET_WORK_GET_SUCCESS;
                }
            }
            sharedPreferences.edit().putString(Config.PREFERENCE_SCHOOL_CALENDAR_URL, imageUrl).apply();
            if (ImageMethod.downloadImage(context, imageUrl, ImageMethod.getSchoolCalendarImagePath(context), false)) {
                return Config.NET_WORK_GET_SUCCESS;
            }
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public Bitmap getSchoolCalendarImage() {
        return ImageMethod.getSchoolCalendarImage(context);
    }
}
