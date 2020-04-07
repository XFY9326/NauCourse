package tool.xfy9326.naucourse.io.db

import okhttp3.Cookie
import okhttp3.HttpUrl
import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.dao.BaseCookieDataDao
import tool.xfy9326.naucourse.io.db.room.NetworkDB
import tool.xfy9326.naucourse.network.beans.CookieData
import tool.xfy9326.naucourse.network.beans.NGXCookieData
import tool.xfy9326.naucourse.network.beans.SSOCookieData

object NetworkDBHelper : BaseDBHelper<NetworkDB.NetworkDataBase>() {
    const val SSO_COOKIES_TABLE_NAME = "SSOCookies"
    const val NGX_COOKIES_TABLE_NAME = "NGXCookies"

    const val COLUMN_HOST = "host"
    const val COLUMN_NAME = "name"

    override val db: NetworkDB.NetworkDataBase = NetworkDB.getDB()

    enum class CookiesType {
        SSO,
        NGX;
    }

    @Synchronized
    fun loadForRequest(url: HttpUrl, type: CookiesType): List<Cookie> =
        when (type) {
            CookiesType.SSO -> loadForRequest(url, db.getSSOCookiesDataDao())
            CookiesType.NGX -> loadForRequest(url, db.getNGXCookiesDataDao())
        }

    private fun loadForRequest(url: HttpUrl, dao: BaseCookieDataDao<*>): List<Cookie> = with(dao) {
        val cookiesDataList = getCookieData(url.host)
        val cookiesList = ArrayList<Cookie>(cookiesDataList.size)
        for (cookiesDatum in cookiesDataList) {
            cookiesList.add(parseCookies(cookiesDatum))
        }
        return cookiesList
    }

    @Synchronized
    fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>, type: CookiesType) =
        when (type) {
            CookiesType.SSO -> saveFromResponse(url, cookies, db.getSSOCookiesDataDao(), type)
            CookiesType.NGX -> saveFromResponse(url, cookies, db.getNGXCookiesDataDao(), type)
        }

    private fun <T : CookieData> saveFromResponse(url: HttpUrl, cookies: List<Cookie>, dao: BaseCookieDataDao<T>, type: CookiesType) = with(dao) {
        for (cookie in cookies) {
            if (cookie.persistent) {
                deleteCookieData(url.host, cookie.name)
            } else {
                putCookieData(parseCookies(url, cookie, type))
            }
        }
        clearPersistentCookieData()
    }

    @Synchronized
    fun clearAllCookies(type: CookiesType) =
        when (type) {
            CookiesType.SSO -> clearAllCookies(db.getSSOCookiesDataDao())
            CookiesType.NGX -> clearAllCookies(db.getNGXCookiesDataDao())
        }

    private fun clearAllCookies(dao: BaseCookieDataDao<*>) = dao.clearAllCookieData()

    @Synchronized
    override fun clearAll() {
        clearAllCookies(db.getSSOCookiesDataDao())
        clearAllCookies(db.getNGXCookiesDataDao())
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T : CookieData> parseCookies(url: HttpUrl, cookie: Cookie, type: CookiesType): T =
        when (type) {
            CookiesType.SSO -> SSOCookieData(
                url.host,
                cookie.name,
                cookie.value,
                cookie.expiresAt,
                cookie.domain,
                cookie.path,
                cookie.secure,
                cookie.httpOnly,
                cookie.hostOnly,
                cookie.persistent
            )
            CookiesType.NGX -> NGXCookieData(
                url.host,
                cookie.name,
                cookie.value,
                cookie.expiresAt,
                cookie.domain,
                cookie.path,
                cookie.secure,
                cookie.httpOnly,
                cookie.hostOnly,
                cookie.persistent
            )
        } as T

    private fun parseCookies(cookieData: CookieData): Cookie = Cookie.Builder().apply {
        path(cookieData.path)
        name(cookieData.name)
        value(cookieData.value)
        expiresAt(cookieData.expiresAt)
        if (cookieData.secure) secure()
        if (cookieData.httpOnly) httpOnly()
        if (cookieData.hostOnly) hostOnlyDomain(cookieData.domain) else domain(cookieData.domain)
    }.build()
}