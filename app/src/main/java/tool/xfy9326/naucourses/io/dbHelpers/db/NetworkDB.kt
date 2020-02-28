package tool.xfy9326.naucourses.io.dbHelpers.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.io.dbHelpers.NetworkDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.base.BaseDB

class NetworkDB private constructor(context: Context) : BaseDB() {
    override val db = Room.databaseBuilder(
        context, NetworkDataBase::class.java,
        NETWORK_DB_NAME
    ).build()

    companion object {
        @Volatile
        private lateinit var instance: NetworkDB

        private const val NETWORK_DB_NAME = "Network.db"
        private const val NETWORK_DB_VERSION = 1

        fun getInstance(): NetworkDB = synchronized(this) {
            if (!::instance.isInitialized) {
                instance = NetworkDB(App.instance)
            }
            instance
        }
    }

    @Database(entities = [NetworkDBHelper.CookieData::class], version = NETWORK_DB_VERSION, exportSchema = false)
    abstract class NetworkDataBase : RoomDatabase() {
        abstract fun getCookiesDataDao(): NetworkDBHelper.CookieDataDao
    }
}