package lib.xfy9326.naujwc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import okhttp3.FormBody;

/**
 * Created by xfy9326 on 18-2-20.
 */

class NauNetData {
    /*@Nullable
    @SuppressWarnings("SameParameterValue")
    static String getCheckCodeUrl(String server, @NonNull String data) {
        String CheckCodeUrl = null;
        Pattern pattern = Pattern.compile("CheckCode.aspx\\?\\d*");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            CheckCodeUrl = server + "/" + matcher.group().trim();
        }
        return CheckCodeUrl;
    }*/

    /*@Nullable
    static String getCheckCode(@NonNull String data) {
        String checkcode = null;
        Pattern pattern = Pattern.compile("CheckCode=\\d*");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.group();
            checkcode = data.substring(data.indexOf("=") + 1);
        }
        return checkcode;
    }*/

    static FormBody getSSOPostForm(String userId, String userPw, String ssoContent) {
        String lt = null, execution = null, eventId = null, useVCode = null, isUseVCode = null, sessionVcode = null, errorCount = null;
        Document document = Jsoup.parse(ssoContent);
        Elements nameList = document.select("input[name]");
        for (Element element : nameList) {
            String value = element.attr("value");
            switch (element.attr("name")) {
                case "lt":
                    lt = value;
                    break;
                case "execution":
                    execution = value;
                    break;
                case "_eventId":
                    eventId = value;
                    break;
                case "useVCode":
                    useVCode = value;
                    break;
                case "isUseVCode":
                    isUseVCode = value;
                    break;
                case "sessionVcode":
                    sessionVcode = value;
                    break;
                case "errorCount":
                    errorCount = value;
                    break;
            }
        }

        if (lt != null && execution != null && eventId != null && useVCode != null && isUseVCode != null && sessionVcode != null && errorCount != null) {
            FormBody.Builder form_builder = new FormBody.Builder();
            form_builder.add("username", userId);
            form_builder.add("password", userPw);

            form_builder.add("lt", lt);
            form_builder.add("execution", execution);
            form_builder.add("_eventId", eventId);
            form_builder.add("useVCode", userId);
            form_builder.add("isUseVCode", isUseVCode);
            form_builder.add("sessionVcode", sessionVcode);
            form_builder.add("errorCount", errorCount);

            return form_builder.build();
        } else {
            return null;
        }
    }
}
