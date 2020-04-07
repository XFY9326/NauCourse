package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import tool.xfy9326.naucourse.network.beans.CookieData

interface BaseCookieDataDao<T : CookieData> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCookieData(cookieData: T)

    fun getCookieData(host: String): Array<T>

    fun deleteCookieData(host: String, name: String)

    fun clearPersistentCookieData()

    fun clearAllCookieData()
}