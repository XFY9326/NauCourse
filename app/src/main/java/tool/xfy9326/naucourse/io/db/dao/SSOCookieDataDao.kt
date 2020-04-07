package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Query
import tool.xfy9326.naucourse.io.db.NetworkDBHelper
import tool.xfy9326.naucourse.network.beans.SSOCookieData

@Dao
interface SSOCookieDataDao :
    BaseCookieDataDao<SSOCookieData> {
    @Query("select * from ${NetworkDBHelper.SSO_COOKIES_TABLE_NAME} where ${NetworkDBHelper.COLUMN_HOST} = :host")
    override fun getCookieData(host: String): Array<SSOCookieData>

    @Query("delete from ${NetworkDBHelper.SSO_COOKIES_TABLE_NAME} where ${NetworkDBHelper.COLUMN_HOST} = :host and ${NetworkDBHelper.COLUMN_NAME} = :name")
    override fun deleteCookieData(host: String, name: String)

    @Query("delete from ${NetworkDBHelper.SSO_COOKIES_TABLE_NAME} where persistent = 1")
    override fun clearPersistentCookieData()

    @Query("delete from ${NetworkDBHelper.SSO_COOKIES_TABLE_NAME}")
    override fun clearAllCookieData()
}