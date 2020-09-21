package tool.xfy9326.naucourse.network.utils

import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class TempCookieStore : BaseCookieStore {
    private val cookieMap: ConcurrentHashMap<String, ConcurrentHashMap<String, Cookie>> = ConcurrentHashMap()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        for (cookie in cookies) {
            if (cookie.persistent) {
                if (cookieMap.containsKey(host)) {
                    cookieMap[host]?.remove(cookie.name)
                }
            } else {
                if (!cookieMap.containsKey(host)) {
                    cookieMap[host] = ConcurrentHashMap(cookies.size)
                }
                cookieMap[host]?.put(cookie.name, cookie)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl) = cookieMap[url.host]?.values?.toList() ?: emptyList()

    override fun clearCookies() = cookieMap.clear()
}