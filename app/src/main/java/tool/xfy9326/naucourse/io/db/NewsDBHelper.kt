package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.AppDB
import tool.xfy9326.naucourse.providers.beans.GeneralNews

object NewsDBHelper : BaseDBHelper<AppDB.AppDataBase>() {
    const val NEWS_TABLE_NAME = "News"
    const val COLUMN_POST_DATE = "postDate"
    const val COLUMN_POST_SOURCE = "postSource"
    const val COLUMN_POST_SOURCE_UNKNOWN = "UNKNOWN"

    override val db: AppDB.AppDataBase = AppDB.getDB()

    @Synchronized
    fun putGeneralNewsSet(newsSet: List<GeneralNews>) = db.getNewsDataDao().putNews(*newsSet.toTypedArray())

    @Synchronized
    fun getGeneralNewsArray(): List<GeneralNews> = db.getNewsDataDao().getFixedAllNews()

    @Synchronized
    override fun clearAll() = db.getNewsDataDao().clearTable()
}