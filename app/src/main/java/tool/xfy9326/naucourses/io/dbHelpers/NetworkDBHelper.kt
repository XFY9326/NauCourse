package tool.xfy9326.naucourses.io.dbHelpers

import androidx.room.*
import okhttp3.Cookie
import okhttp3.HttpUrl
import tool.xfy9326.naucourses.io.dbHelpers.db.NetworkDB

object NetworkDBHelper {
    private const val SSO_COOKIES_TABLE_NAME = "SSOCookies"
    private const val NGX_COOKIES_TABLE_NAME = "NGXCookies"

    private const val COLUMN_HOST = "host"
    private const val COLUMN_NAME = "name"

    private val netWorkDB = NetworkDB.db

    enum class CookiesType {
        SSO,
        NGX;
    }

    @Synchronized
    fun loadForRequest(url: HttpUrl, type: CookiesType): List<Cookie> =
        when (type) {
            CookiesType.SSO -> loadForRequest(url, netWorkDB.getSSOCookiesDataDao())
            CookiesType.NGX -> loadForRequest(url, netWorkDB.getNGXCookiesDataDao())
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
            CookiesType.SSO -> saveFromResponse(url, cookies, netWorkDB.getSSOCookiesDataDao(), type)
            CookiesType.NGX -> saveFromResponse(url, cookies, netWorkDB.getNGXCookiesDataDao(), type)
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

    fun clearAllCookies(type: CookiesType) =
        when (type) {
            CookiesType.SSO -> clearAllCookies(netWorkDB.getSSOCookiesDataDao())
            CookiesType.NGX -> clearAllCookies(netWorkDB.getNGXCookiesDataDao())
        }

    private fun clearAllCookies(dao: BaseCookieDataDao<*>) = dao.clearAllCookieData()

    fun clearAll() {
        clearAllCookies(netWorkDB.getSSOCookiesDataDao())
        clearAllCookies(netWorkDB.getNGXCookiesDataDao())
    }

    @Entity(tableName = NGX_COOKIES_TABLE_NAME, primaryKeys = [COLUMN_HOST, COLUMN_NAME])
    data class NGXCookieData(
        @ColumnInfo(name = COLUMN_HOST)
        override val host: String,
        @ColumnInfo(name = COLUMN_NAME)
        override val name: String,
        override val value: String,
        override val expiresAt: Long,
        override val domain: String,
        override val path: String,
        override val secure: Boolean,
        override val httpOnly: Boolean,
        override val hostOnly: Boolean,
        override val persistent: Boolean
    ) : CookieData()


    @Entity(tableName = SSO_COOKIES_TABLE_NAME, primaryKeys = [COLUMN_HOST, COLUMN_NAME])
    data class SSOCookieData(
        @ColumnInfo(name = COLUMN_HOST)
        override val host: String,
        @ColumnInfo(name = COLUMN_NAME)
        override val name: String,
        override val value: String,
        override val expiresAt: Long,
        override val domain: String,
        override val path: String,
        override val secure: Boolean,
        override val httpOnly: Boolean,
        override val hostOnly: Boolean,
        override val persistent: Boolean
    ) : CookieData()

    abstract class CookieData {
        abstract val host: String
        abstract val name: String
        abstract val value: String
        abstract val expiresAt: Long
        abstract val domain: String
        abstract val path: String
        abstract val secure: Boolean
        abstract val httpOnly: Boolean
        abstract val hostOnly: Boolean
        abstract val persistent: Boolean
    }

    @Dao
    interface SSOCookieDataDao : BaseCookieDataDao<SSOCookieData> {
        @Query("select * from $SSO_COOKIES_TABLE_NAME where $COLUMN_HOST = :host")
        override fun getCookieData(host: String): Array<SSOCookieData>

        @Query("delete from $SSO_COOKIES_TABLE_NAME where $COLUMN_HOST = :host and $COLUMN_NAME = :name")
        override fun deleteCookieData(host: String, name: String)

        @Query("delete from $SSO_COOKIES_TABLE_NAME where persistent = 1")
        override fun clearPersistentCookieData()

        @Query("delete from $SSO_COOKIES_TABLE_NAME")
        override fun clearAllCookieData()
    }

    @Dao
    interface NGXCookieDataDao : BaseCookieDataDao<NGXCookieData> {
        @Query("select * from $NGX_COOKIES_TABLE_NAME where $COLUMN_HOST = :host")
        override fun getCookieData(host: String): Array<NGXCookieData>

        @Query("delete from $NGX_COOKIES_TABLE_NAME where $COLUMN_HOST = :host and $COLUMN_NAME = :name")
        override fun deleteCookieData(host: String, name: String)

        @Query("delete from $NGX_COOKIES_TABLE_NAME where persistent = 1")
        override fun clearPersistentCookieData()

        @Query("delete from $NGX_COOKIES_TABLE_NAME")
        override fun clearAllCookieData()
    }

    interface BaseCookieDataDao<T : CookieData> {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putCookieData(cookieData: T)

        fun getCookieData(host: String): Array<T>

        fun deleteCookieData(host: String, name: String)

        fun clearPersistentCookieData()

        fun clearAllCookieData()
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