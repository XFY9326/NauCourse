package tool.xfy9326.naucourses.io.dbHelpers

import androidx.room.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.db.AppDB
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.info.methods.NewsInfo

object AppDBHelper {
    const val NEWS_TABLE_NAME = "News"
    const val COLUMN_POST_DATE = "postDate"
    const val COLUMN_ID = "id"

    private val appDB = AppDB.getInstance().db

    fun putGeneralNewsSet(newsSet: List<GeneralNews>) = with(appDB.getNewsDataDao()) {
        putNews(*newsSet.toTypedArray())
    }

    @Synchronized
    fun getGeneralNewsArray(): List<GeneralNews> = with(appDB.getNewsDataDao()) {
        clearOutOfDateNews(NewsInfo.isNewsOutOfDateTimeStamp())
        val newsArray = getAllNews()
        newsArray.forEach {
            if (it.postSource == GeneralNews.PostSource.UNKNOWN) {
                newsArray.remove(it)
                deleteNews(it)
            }
        }
        newsArray
    }

    fun clearGeneralNewsByType(type: GeneralNews.PostSource) = with(appDB.getNewsDataDao()) {
        clearNewsByType(type.name)
    }

    @Synchronized
    fun clearGeneralNewsSet() = with(appDB.getNewsDataDao()) {
        clearAllNews()
        clearIndex()
    }

    @Dao
    interface NewsDataDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putNews(vararg news: GeneralNews)

        @Query("select * from $NEWS_TABLE_NAME order by $COLUMN_POST_DATE desc")
        fun getAllNews(): MutableList<GeneralNews>

        @Delete
        fun deleteNews(news: GeneralNews)

        @Query("delete from $NEWS_TABLE_NAME where $COLUMN_POST_DATE < :timeStamp")
        fun clearOutOfDateNews(timeStamp: Long)

        @Query("delete from $NEWS_TABLE_NAME where $COLUMN_POST_DATE < :type")
        fun clearNewsByType(type: String)

        @Query("delete from $NEWS_TABLE_NAME")
        fun clearAllNews()

        @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '${NEWS_TABLE_NAME}'")
        fun clearIndex()
    }
}