package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;

/**
 * Created by xfy9326 on 18-2-20.
 * 登陆登出以及获取数据方法
 */

public class LoginMethod {

    static String getData(Context context, String url) {
        try {
            return BaseMethod.getBaseApplication(context).getClient().getUserData(url);
        } catch (Exception e) {
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(context, R.string.network_get_error, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
        return null;
    }

    public static boolean loginOut(Context context) {
        try {
            boolean result = false;
            BaseMethod.getBaseApplication(context).getClient().loginOut();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().remove(Config.PREFERENCE_LOGIN_URL).putBoolean(Config.PREFERENCE_HAS_LOGIN, false).apply();
            File cache_file = context.getCacheDir();
            for (File file : cache_file.listFiles()) {
                result = file.delete();
            }
            File data_file = context.getFilesDir();
            for (File file : data_file.listFiles()) {
                result = file.delete();
            }
            Log.d("LOGIN_OUT_CLEAR", String.valueOf(result));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
