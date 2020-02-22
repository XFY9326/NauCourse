package tool.xfy9326.naucourses.io.dbHelpers.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tool.xfy9326.naucourses.io.dbHelpers.AppDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.base.BaseDB
import tool.xfy9326.naucourses.io.dbHelpers.base.DBTypeConverter
import tool.xfy9326.naucourses.providers.beans.GeneralNews

class AppDB private constructor(context: Context) : BaseDB() {
    override val db = Room.databaseBuilder(
        context, AppDataBase::class.java,
        APP_DB_NAME
    ).build()

    companion object {
        @Volatile
        private lateinit var instance: AppDB

        private const val APP_DB_NAME = "App.db"
        private const val APP_DB_VERSION = 1

        fun initInstance(context: Context) = synchronized(this) {
            if (!Companion::instance.isInitialized) {
                instance = AppDB(context)
            }
        }

        fun getInstance(): AppDB = instance
    }

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