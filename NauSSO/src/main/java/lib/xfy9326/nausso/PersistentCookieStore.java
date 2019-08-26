package lib.xfy9326.nausso;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * Created by 10696 on 2018/2/25.
 * 用于Cookie自动持久化
 * From http://www.codeceo.com/article/okhttp3-cookies-manage.html
 */

class PersistentCookieStore {
    private static final String LOG_TAG = "PersistentCookieStore";
    private static final String COOKIE_PREFS = "Cookies_Prefs";

    @NonNull
    private final Map<String, ConcurrentHashMap<String, Cookie>> cookies;
    private final SharedPreferences cookiePrefs;

    PersistentCookieStore(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Activity.MODE_PRIVATE);
        cookies = new HashMap<>();

        //将持久化的cookies缓存到内存中 即map cookies
        Map<String, ?> prefsMap = cookiePrefs.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            String[] cookieNames = TextUtils.split((String) entry.getValue(), ",");
            for (String name : cookieNames) {
                String encodedCookie = cookiePrefs.getString(name, null);
                if (encodedCookie != null) {
                    Cookie decodedCookie = decodeCookie(encodedCookie);
                    if (decodedCookie != null) {
                        if (!cookies.containsKey(entry.getKey())) {
                            cookies.put(entry.getKey(), new ConcurrentHashMap<>());
                        }
                        ConcurrentHashMap<String, Cookie> concurrentHashMap = cookies.get(entry.getKey());
                        if (concurrentHashMap != null) {
                            concurrentHashMap.put(name, decodedCookie);
                        }
                    }
                }
            }
        }
    }

    private String getCookieToken(Cookie cookie) {
        return cookie.name() + "@" + cookie.domain();
    }

    public void add(@NonNull HttpUrl url, @NonNull Cookie cookie) {
        String name = getCookieToken(cookie);

        //将cookies缓存到内存中 如果缓存过期 就重置此cookie
        if (!cookie.persistent()) {
            if (!cookies.containsKey(url.host())) {
                cookies.put(url.host(), new ConcurrentHashMap<>(10));
            }
            ConcurrentHashMap<String, Cookie> concurrentHashMap = cookies.get(url.host());
            if (concurrentHashMap != null) {
                concurrentHashMap.put(name, cookie);
            }
        } else {
            if (cookies.containsKey(url.host())) {
                ConcurrentHashMap<String, Cookie> concurrentHashMap = cookies.get(url.host());
                if (concurrentHashMap != null) {
                    concurrentHashMap.remove(name);
                }
            }
        }

        //cookies持久化到本地
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        ConcurrentHashMap<String, Cookie> concurrentHashMap = cookies.get(url.host());
        if (concurrentHashMap != null) {
            prefsWriter.putString(url.host(), TextUtils.join(",", concurrentHashMap.keySet()));
        }
        prefsWriter.putString(name, encodeCookie(new SerializableOkHttpCookies(cookie)));
        prefsWriter.apply();
    }

    @NonNull
    List<Cookie> get(@NonNull HttpUrl url) {
        ArrayList<Cookie> ret = new ArrayList<>();
        if (cookies.containsKey(url.host())) {
            ConcurrentHashMap<String, Cookie> concurrentHashMap = cookies.get(url.host());
            if (concurrentHashMap != null) {
                ret.addAll(concurrentHashMap.values());
            }
        }
        return ret;
    }

    @SuppressWarnings("SameReturnValue")
    boolean removeAll() {
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.clear();
        prefsWriter.apply();
        cookies.clear();
        return true;
    }

    @SuppressWarnings("unused")
    public boolean remove(@NonNull HttpUrl url, @NonNull Cookie cookie) {
        String name = getCookieToken(cookie);
        ConcurrentHashMap<String, Cookie> concurrentHashMap = cookies.get(url.host());
        if (cookies.containsKey(url.host()) && concurrentHashMap != null && concurrentHashMap.containsKey(name)) {
            concurrentHashMap = cookies.get(url.host());
            if (concurrentHashMap != null) {
                concurrentHashMap.remove(name);
                SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
                if (cookiePrefs.contains(name)) {
                    prefsWriter.remove(name);
                }
                concurrentHashMap = cookies.get(url.host());
                if (concurrentHashMap != null) {
                    prefsWriter.putString(url.host(), TextUtils.join(",", concurrentHashMap.keySet()));
                }
                prefsWriter.apply();
                return true;
            }
        }
        return false;
    }

    @NonNull
    @SuppressWarnings("unused")
    public List<Cookie> getCookies() {
        ArrayList<Cookie> ret = new ArrayList<>();
        ConcurrentHashMap<String, Cookie> concurrentHashMap;
        for (String key : cookies.keySet()) {
            concurrentHashMap = cookies.get(key);
            if (concurrentHashMap != null) {
                ret.addAll(concurrentHashMap.values());
            }
        }
        return ret;
    }

    /**
     * cookies 序列化成 string
     *
     * @param cookie 要序列化的cookie
     * @return 序列化之后的string
     */
    @Nullable
    private String encodeCookie(@Nullable SerializableOkHttpCookies cookie) {
        if (cookie == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in encodeCookie", e);
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    /**
     * 将字符串反序列化成cookies
     *
     * @param cookieString cookies string
     * @return cookie object
     */
    @Nullable
    private Cookie decodeCookie(@NonNull String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableOkHttpCookies) objectInputStream.readObject()).getCookies();
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in decodeCookie", e);
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, "ClassNotFoundException in decodeCookie", e);
        }

        return cookie;
    }

    /**
     * 二进制数组转十六进制字符串
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * 十六进制字符串转二进制数组
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    @NonNull
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
