package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.NetMethod;

/**
 * Created by 10696 on 2018/2/28.
 * 获取教务信息
 */

public class SchoolCalendarMethod extends BaseNetMethod {
    private static final String server_url = "http://jw.nau.edu.cn";
    private static final String calendar_server_utl = "https://www.nau.edu.cn";
    private static final String calendar_list = "https://www.nau.edu.cn/p141c89/list.htm";
    private final SharedPreferences sharedPreferences;
    @Nullable
    private Document document;
    @Nullable
    private Document document_list;

    public SchoolCalendarMethod(@NonNull Context context) {
        super(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int load() throws Exception {
        String data = NetMethod.loadUrl(context, server_url);
        if (data != null) {
            document = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public int loadCalendarList() throws Exception {
        String data = NetMethod.loadUrl(context, calendar_list);
        if (data != null) {
            document_list = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    @Nullable
    public LinkedHashMap<String, String> getCalendarUrlList() {
        LinkedHashMap<String, String> calendarList = new LinkedHashMap<>();
        if (document_list != null) {
            Elements elements = document_list.select("span[class=cols_title]");
            for (Element element : elements) {
                Elements a_arr = element.getElementsByTag("a");
                if (a_arr != null && a_arr.size() != 0) {
                    Element a = a_arr.first();
                    String name = a.text().trim();
                    String url = a.attr("href").trim();
                    calendarList.put(name, calendar_server_utl + url);
                }
            }
        }
        return calendarList;
    }

    public int loadSchoolCalendarImage(boolean checkTemp) throws Exception {
        String url = sharedPreferences.getString(Config.PREFERENCE_SCHOOL_CALENDAR_PAGE_URL, null);
        if (url == null) {
            return loadCurrentSchoolCalendarImage(checkTemp);
        } else {
            return saveImageUrl(url, checkTemp);
        }
    }

    private int loadCurrentSchoolCalendarImage(boolean checkTemp) throws Exception {
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
                return saveImageUrl(url, checkTemp);
            }
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    private int saveImageUrl(String imagePageUrl, boolean checkTemp) throws Exception {
        String imageUrl = null;
        if (imagePageUrl != null) {
            String data = NetMethod.loadUrl(context, imagePageUrl);
            if (data != null) {
                Document document = Jsoup.parse(data);
                Elements elements_img = document.select("div[class=wp_articlecontent]");
                Element element = elements_img.get(0).getElementsByTag("img").first();
                if (element.hasAttr("src")) {
                    imageUrl = calendar_server_utl + element.attr("src");
                }
            }
        }
        if (imageUrl != null) {
            if (checkTemp) {
                String old_url = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_SCHOOL_CALENDAR_URL, null);
                if (old_url != null && old_url.equalsIgnoreCase(imageUrl)) {
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
