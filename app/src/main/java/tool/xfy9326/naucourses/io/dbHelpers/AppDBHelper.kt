package tool.xfy9326.naucourses.io.dbHelpers

import androidx.room.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.db.AppDB
import tool.xfy9326.naucourses.providers.beans.GeneralNews

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
        clearOutOfDateNews(System.currentTimeMillis() - Constants.News.NEWS_STORE_DAY_LENGTH * 24 * 60 * 60 * 1000L)
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

    fun clearGeneralNewsSet() = with(appDB.getNewsDataDao()) {
        clearAllNews()
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
    }
}