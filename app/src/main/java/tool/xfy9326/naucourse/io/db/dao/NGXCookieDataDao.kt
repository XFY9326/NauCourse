package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Query
import tool.xfy9326.naucourse.io.db.NetworkDBHelper
import tool.xfy9326.naucourse.network.beans.NGXCookieData

@Dao
interface NGXCookieDataDao :
    BaseCookieDataDao<NGXCookieData> {
    @Query("select * from ${NetworkDBHelper.NGX_COOKIES_TABLE_NAME} where ${NetworkDBHelper.COLUMN_HOST} = :host")
    override fun getCookieData(host: String): Array<NGXCookieData>

    @Query("delete from ${NetworkDBHelper.NGX_COOKIES_TABLE_NAME} where ${NetworkDBHelper.COLUMN_HOST} = :host and ${NetworkDBHelper.COLUMN_NAME} = :name")
    override fun deleteCookieData(host: String, name: String)

    @Query("delete from ${NetworkDBHelper.NGX_COOKIES_TABLE_NAME} where persistent = 1")
    override fun clearPersistentCookieData()

    @Query("delete from ${NetworkDBHelper.NGX_COOKIES_TABLE_NAME}")
    override fun clearAllCookieData()
}