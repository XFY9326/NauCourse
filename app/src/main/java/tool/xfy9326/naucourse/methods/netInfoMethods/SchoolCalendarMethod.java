package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.NetMethod;

/**
 * Created by 10696 on 2018/2/28.
 * 获取教务信息
 */

public class SchoolCalendarMethod extends BaseNetMethod {
    private static final String server_url = "http://jw.nau.edu.cn";
    private static final String calendar_server_utl = "http://www.nau.edu.cn";
    @Nullable
    private Document document;

    public SchoolCalendarMethod(@NonNull Context context) {
        super(context);
        this.document = null;
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
                String data = NetMethod.loadUrl(context, url);
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
            if (ImageMethod.downloadImage(context, Image_Url, ImageMethod.getSchoolCalendarImagePath(context), false)) {
                return Config.NET_WORK_GET_SUCCESS;
            }
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public Bitmap getSchoolCalendarImage() {
        return ImageMethod.getSchoolCalendarImage(context);
    }
}
