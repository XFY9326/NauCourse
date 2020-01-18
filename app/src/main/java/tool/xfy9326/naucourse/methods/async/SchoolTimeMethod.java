package tool.xfy9326.naucourse.methods.async;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.SchoolTime;
import tool.xfy9326.naucourse.methods.io.DataMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.nausso.NauSSOClient;

/**
 * Created by xfy9326 on 18-2-22.
 * 学期时间获取方法
 */

public class SchoolTimeMethod extends BaseInfoMethod<SchoolTime> {
    public static final String FILE_NAME = SchoolTime.class.getSimpleName();
    public static final boolean IS_ENCRYPT = false;
    @Nullable
    private Document document;

    public SchoolTimeMethod(@NonNull Context context) {
        super(context);
    }

    @Override
    public int load() throws IOException, InterruptedException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String url = sharedPreferences.getString(Config.PREFERENCE_LOGIN_URL, null);
            if (url != null) {
                String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + url, true);
                if (NauSSOClient.checkUserLogin(Objects.requireNonNull(data))) {
                    document = Jsoup.parse(data);
                    return Config.NET_WORK_GET_SUCCESS;
                }
                return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
            }
            return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
        }
        return Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN;
    }

    //调用该方法的同时，需要调用TimeMethod中的termSetCheck方法获取真实的日期
    @Nullable
    @Override
    public SchoolTime getData(boolean checkTemp) {
        SchoolTime schoolTime = new SchoolTime();
        Element element = Objects.requireNonNull(document).getElementById("TermInfo");
        if (element != null) {
            Elements elements = element.getElementsByTag("span");
            List<String> text = elements.eachText();
            for (int i = 0; i < text.size(); i++) {
                switch (i) {
                    case 2:
                        if (!"放假中".equals(text.get(i))) {
                            schoolTime.setWeekNum(Integer.valueOf(text.get(i).trim().substring(1, 2)));
                        }
                        break;
                    case 3:
                        schoolTime.setStartTime(text.get(i));
                        break;
                    case 4:
                        schoolTime.setEndTime(text.get(i));
                        break;
                    default:
                }
            }

            schoolTime.setDataVersionCode(Config.DATA_VERSION_SCHOOL_TIME);
            if (DataMethod.saveOfflineData(context, schoolTime, FILE_NAME, false, IS_ENCRYPT)) {
                return schoolTime;
            }
        }
        return null;
    }
}
