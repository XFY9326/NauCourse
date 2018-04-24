package lib.xfy9326.naujwc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xfy9326 on 18-2-20.
 */

class NauNetData {
    @Nullable
    @SuppressWarnings("SameParameterValue")
    static String getCheckCodeUrl(String server, @NonNull String data) {
        String CheckCodeUrl = null;
        Pattern pattern = Pattern.compile("CheckCode.aspx\\?\\d*");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            CheckCodeUrl = server + "/" + matcher.group().trim();
        }
        return CheckCodeUrl;
    }

    @Nullable
    static String getCheckCode(@NonNull String data) {
        String checkcode = null;
        Pattern pattern = Pattern.compile("CheckCode=\\d*");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.group();
            checkcode = data.substring(data.indexOf("=") + 1);
        }
        return checkcode;
    }
}
