package tool.xfy9326.naucourse.methods.async;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.net.NetMethod;

public class StudentCardMethod extends BaseInfoMethod<String> {
    private static final String MAIN_URL = "http://my1.nau.edu.cn/";
    private static final String API_URL = "http://my1.nau.edu.cn/_web/_plugs/notes/selectNotesDefById.rst?id=1";
    private String cardContent = null;
    private SharedPreferences sharedPreferences;

    public StudentCardMethod(@NonNull Context context) {
        super(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int load() throws Exception {
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, MAIN_URL, true);
            if (data != null) {
                if (NauSSOClient.checkUserLogin(data)) {
                    String content = NetMethod.loadUrlFromLoginClient(context, API_URL, true);
                    if (content != null && content.contains("一卡通") && !content.contains("根据id查找提醒设置出错")) {
                        cardContent = content;
                        return Config.NET_WORK_GET_SUCCESS;
                    } else {
                        return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
                    }
                }
                return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
            }
            return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
        }
        return Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN;
    }

    @Nullable
    @Override
    public String getData(boolean checkTemp) {
        String result = null;
        if (cardContent != null) {
            try {
                JSONObject jsonObject = new JSONObject(cardContent);
                result = jsonObject.getString("count");
                sharedPreferences.edit().putString(Config.PREFERENCE_STUDENT_CARD_MONEY, result).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
