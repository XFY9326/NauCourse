package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by xfy9326 on 18-2-22.
 * 学期时间获取方法
 */

public class SchoolTimeMethod {
    public static final String FILE_NAME = "SchoolTime";
    private final Context context;
    private Document document;

    public SchoolTimeMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String url = sharedPreferences.getString(Config.PREFERENCE_LOGIN_URL, null);
            if (url != null) {
                String data = LoginMethod.getData(context, url, true);
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

    public SchoolTime getSchoolTime() {
        SchoolTime schoolTime = new SchoolTime();
        Element element = document.getElementById("TermInfo");
        if (element != null) {
            Elements elements = element.getElementsByTag("span");
            List<String> text = elements.eachText();
            for (int i = 0; i < text.size(); i++) {
                switch (i) {
                    case 2:
                        if (!text.get(i).equals("放假中")) {
                            schoolTime.setWeekNum(Integer.valueOf(text.get(i).trim().substring(1, 2)));
                        }
                        break;
                    case 3:
                        schoolTime.setStartTime(text.get(i));
                        break;
                    case 4:
                        schoolTime.setEndTime(text.get(i));
                        break;
                }
            }

            schoolTime.setDataVersionCode(Config.DATA_VERSION_SCHOOL_TIME);
            if (DataMethod.saveOfflineData(context, schoolTime, FILE_NAME, false)) {
                return schoolTime;
            }
        }
        return null;
    }
}
