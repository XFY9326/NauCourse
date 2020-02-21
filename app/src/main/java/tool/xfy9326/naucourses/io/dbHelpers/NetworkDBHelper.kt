package tool.xfy9326.naucourses.io.dbHelpers

import androidx.room.*
import okhttp3.Cookie
import okhttp3.HttpUrl
import tool.xfy9326.naucourses.io.dbHelpers.db.NetworkDB

object NetworkDBHelper {
    private const val TABLE_NAME = "SSOCookies"
    private const val COLUMN_HOST = "host"
    private const val COLUMN_NAME = "name"

    private val netWorkDB = NetworkDB.getInstance().db

    @Synchronized
    fun loadForRequest(url: HttpUrl): List<Cookie> = with(netWorkDB.getCookiesDataDao()) {
        val cookiesDataList = getCookieData(url.host)
        val cookiesList = ArrayList<Cookie>(cookiesDataList.size)
        for (cookiesDatum in cookiesDataList) {
            cookiesList.add(CookieData.parse(cookiesDatum))
        }
        return cookiesList
    }

    @Synchronized
    fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = with(netWorkDB.getCookiesDataDao()) {
        for (cookie in cookies) {
            if (cookie.persistent) {
                deleteCookieData(url.host, cookie.name)
            } else {
                putCookieData(CookieData.parse(url, cookie))
            }
        }
        clearPersistentCookieData()
    }

    fun clearAllCookies() = with(netWorkDB.getCookiesDataDao()) {
        clearAllCookieData()
    }

    @Entity(tableName = TABLE_NAME, primaryKeys = [COLUMN_HOST, COLUMN_NAME])
    data class CookieData(
        @ColumnInfo(name = COLUMN_HOST)
        val host: String,
        @ColumnInfo(name = COLUMN_NAME)
        val name: String,
        val value: String,
        val expiresAt: Long,
        val domain: String,
        val path: String,
        val secure: Boolean,
        val httpOnly: Boolean,
        val hostOnly: Boolean,
        val persistent: Boolean
    ) {
        companion object {
            fun parse(url: HttpUrl, cookie: Cookie): CookieData =
                CookieData(
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

            fun parse(cookieData: CookieData): Cookie = Cookie.Builder().apply {
                path(cookieData.path)
                name(cookieData.name)
                value(cookieData.value)
                expiresAt(cookieData.expiresAt)
                if (cookieData.secure) secure()
                if (cookieData.httpOnly) httpOnly()
                if (cookieData.hostOnly) hostOnlyDomain(cookieData.domain) else domain(cookieData.domain)
            }.build()
        }
    }

    @Dao
    interface CookieDataDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putCookieData(vararg cookieData: CookieData)

        @Query("select * from $TABLE_NAME where $COLUMN_HOST = :host")
        fun getCookieData(host: String): Array<CookieData>

        @Query("delete from $TABLE_NAME where $COLUMN_HOST = :host and $COLUMN_NAME = :name")
        fun deleteCookieData(host: String, name: String)

        @Query("delete from $TABLE_NAME where persistent = 1")
        fun clearPersistentCookieData()

        @Query("delete from $TABLE_NAME")
        fun clearAllCookieData()
    }
}