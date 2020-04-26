package tool.xfy9326.naucourse.network.utils

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import tool.xfy9326.naucourse.io.db.NetworkDBHelper

class CookieStore(private val type: NetworkDBHelper.CookiesType) : CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> = NetworkDBHelper.loadForRequest(url, type)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = NetworkDBHelper.saveFromResponse(url, cookies, type)

    fun clearCookies() = NetworkDBHelper.clearAllCookies(type)
}