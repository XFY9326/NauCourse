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

    static FormBody getSSOPostForm(String userId, String userPw, String ssoContent) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username", userId);
        formBuilder.add("password", userPw);

        Document document = Jsoup.parse(ssoContent);
        Elements nameList = document.select("input[name]");
        for (Element element : nameList) {
            String value = element.attr("value");
            String name = element.attr("name");
            if ("lt".equals(name) || "execution".equals(name) || "_eventId".equals(name) || "useVCode".equals(name) || "isUseVCode".equals(name) || "sessionVcode".equals(name) || "errorCount".equals(name)) {
                formBuilder.add(name, value);
            }
        }

        return formBuilder.build();
    }
}
