package tool.xfy9326.naucourse.io.db.room

import androidx.room.Database
import androidx.room.TypeConverters
import tool.xfy9326.naucourse.beans.UUIDContent
import tool.xfy9326.naucourse.io.db.base.BaseDB
import tool.xfy9326.naucourse.io.db.base.DBTypeConverter
import tool.xfy9326.naucourse.io.db.dao.NewsDataDao
import tool.xfy9326.naucourse.io.db.dao.UUIDDataDao
import tool.xfy9326.naucourse.providers.beans.GeneralNews

object AppDB : BaseDB<AppDB.AppDataBase>() {
    override val dbName: String = "App.db"
    override val dbClass: Class<AppDataBase> = AppDataBase::class.java

    private const val APP_DB_VERSION = 1

    @Database(
        entities = [GeneralNews::class, UUIDContent::class],
        version = APP_DB_VERSION,
        exportSchema = false
    )
    @TypeConverters(DBTypeConverter::class)
    abstract class AppDataBase : DB() {
        abstract fun getNewsDataDao(): NewsDataDao

        abstract fun getUUIDDataDao(): UUIDDataDao

        override fun clearAll() {
            getNewsDataDao().clearIndex()
            super.clearAll()
        }
    }
}