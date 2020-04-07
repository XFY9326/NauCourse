package tool.xfy9326.naucourse.io.db.dao

import androidx.room.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.NewsDBHelper
import tool.xfy9326.naucourse.providers.beans.GeneralNews

@Dao
interface NewsDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putNews(vararg news: GeneralNews)

    @Query("select * from ${NewsDBHelper.NEWS_TABLE_NAME} order by ${NewsDBHelper.COLUMN_POST_DATE} desc")
    fun getAllNews(): MutableList<GeneralNews>

    @Delete
    fun deleteNews(news: GeneralNews)

    @Query("delete from ${NewsDBHelper.NEWS_TABLE_NAME} where ${NewsDBHelper.COLUMN_POST_DATE} < :timeStamp")
    fun clearOutOfDateNews(timeStamp: Long)

    @Query("delete from ${NewsDBHelper.NEWS_TABLE_NAME} where ${NewsDBHelper.COLUMN_POST_DATE} < :type")
    fun clearNewsByType(type: String)

    @Query("delete from ${NewsDBHelper.NEWS_TABLE_NAME}")
    fun clearAllNews()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '${NewsDBHelper.NEWS_TABLE_NAME}'")
    fun clearIndex()
}