package lib.xfy9326.naujwc;

import android.content.Context;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by xfy9326 on 18-2-20.
 */

class CookieStore implements CookieJar {
    @NonNull
    private final PersistentCookieStore cookieStore;

    CookieStore(@NonNull Context context) {
        this.cookieStore = new PersistentCookieStore(context);
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @Nullable List<Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            for (Cookie item : cookies) {
                cookieStore.add(url, item);
            }
        }
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        return cookieStore.get(url);
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean clearCookies() {
        return cookieStore.removeAll();
    }

    @NonNull
    @SuppressWarnings({"SameParameterValue", "unused"})
    List<Cookie> getCookies(@NonNull String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        return cookieStore.get(Objects.requireNonNull(httpUrl));
    }
}