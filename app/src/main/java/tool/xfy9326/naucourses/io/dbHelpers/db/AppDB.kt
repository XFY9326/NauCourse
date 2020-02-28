package tool.xfy9326.naucourses.io.dbHelpers.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.io.dbHelpers.AppDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.base.BaseDB
import tool.xfy9326.naucourses.io.dbHelpers.base.DBTypeConverter
import tool.xfy9326.naucourses.providers.beans.GeneralNews

object AppDB : BaseDB() {
    private const val APP_DB_NAME = "App.db"
    private const val APP_DB_VERSION = 1

    override val db = Room.databaseBuilder(
        App.instance, AppDataBase::class.java,
        APP_DB_NAME
    ).build()

    @Database(
        entities = [GeneralNews::class],
        version = APP_DB_VERSION,
        exportSchema = false
    )
    @TypeConverters(DBTypeConverter::class)
    abstract class AppDataBase : RoomDatabase() {
        abstract fun getNewsDataDao(): AppDBHelper.NewsDataDao
    }
}