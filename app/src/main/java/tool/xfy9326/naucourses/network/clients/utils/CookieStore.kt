package tool.xfy9326.naucourses.network.clients.utils

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import tool.xfy9326.naucourses.io.dbHelpers.NetworkDBHelper


class CookieStore : CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> = NetworkDBHelper.loadForRequest(url)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = NetworkDBHelper.saveFromResponse(url, cookies)

    fun clearCookies() = NetworkDBHelper.clearAllCookies()
}