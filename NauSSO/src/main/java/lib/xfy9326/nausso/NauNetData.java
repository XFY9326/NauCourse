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
