package lib.xfy9326.nausso;

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
        FormBody.Builder form_builder = new FormBody.Builder();
        form_builder.add("username", userId);
        form_builder.add("password", userPw);

        Document document = Jsoup.parse(ssoContent);
        Elements nameList = document.select("input[name]");
        for (Element element : nameList) {
            String value = element.attr("value");
            String name = element.attr("name");
            if ("lt".equals(name) || "execution".equals(name) || "_eventId".equals(name) || "useVCode".equals(name) || "isUseVCode".equals(name) || "sessionVcode".equals(name) || "errorCount".equals(name)) {
                form_builder.add(name, value);
            }
        }

        return form_builder.build();
    }
}
