package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.AppDB
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.info.methods.NewsInfo

object NewsDBHelper : BaseDBHelper<AppDB.AppDataBase>() {
    const val NEWS_TABLE_NAME = "News"
    const val COLUMN_POST_DATE = "postDate"
    const val COLUMN_ID = "id"

    override val db: AppDB.AppDataBase = AppDB.getDB()

    @Synchronized
    fun putGeneralNewsSet(newsSet: List<GeneralNews>) = with(db.getNewsDataDao()) {
        putNews(*newsSet.toTypedArray())
    }

    @Synchronized
    fun getGeneralNewsArray(): List<GeneralNews> = with(db.getNewsDataDao()) {
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

    @Synchronized
    override fun clearAll() = with(db.getNewsDataDao()) {
        clearAllNews()
        clearIndex()
    }
}