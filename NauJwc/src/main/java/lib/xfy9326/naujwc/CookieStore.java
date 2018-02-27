package lib.xfy9326.naujwc;

import android.content.Context;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by xfy9326 on 18-2-20.
 */

class CookieStore implements CookieJar {
    private final PersistentCookieStore cookieStore;

    CookieStore(Context context) {
        this.cookieStore = new PersistentCookieStore(context);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            for (Cookie item : cookies) {
                cookieStore.add(url, item);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return cookieStore.get(url);
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean clearCookies() {
        return cookieStore.removeAll();
    }

    @SuppressWarnings("SameParameterValue")
    List<Cookie> getCookies(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        return cookieStore.get(httpUrl);
    }
}