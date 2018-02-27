package lib.xfy9326.naujwc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xfy9326 on 18-2-20.
 */

class NauNetData {
    @SuppressWarnings("SameParameterValue")
    static String getCheckCodeUrl(String server, String data) {
        String CheckCodeUrl = null;
        Pattern pattern = Pattern.compile("CheckCode.aspx\\?\\d*");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            CheckCodeUrl = server + "/" + matcher.group().trim();
        }
        return CheckCodeUrl;
    }

    static String getCheckCode(String data) {
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
