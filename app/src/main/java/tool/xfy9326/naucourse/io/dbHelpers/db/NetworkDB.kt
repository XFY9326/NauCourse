package tool.xfy9326.naucourse.io.dbHelpers.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.dbHelpers.NetworkDBHelper
import tool.xfy9326.naucourse.io.dbHelpers.base.BaseDB

object NetworkDB : BaseDB() {
    private const val NETWORK_DB_NAME = "Network.db"
    private const val NETWORK_DB_VERSION = 1

    override val db = Room.databaseBuilder(
        App.instance, NetworkDataBase::class.java,
        NETWORK_DB_NAME
    ).build()

    @Database(
        entities = [NetworkDBHelper.SSOCookieData::class, NetworkDBHelper.NGXCookieData::class],
        version = NETWORK_DB_VERSION,
        exportSchema = false
    )
    abstract class NetworkDataBase : RoomDatabase() {
        abstract fun getSSOCookiesDataDao(): NetworkDBHelper.SSOCookieDataDao

        abstract fun getNGXCookiesDataDao(): NetworkDBHelper.NGXCookieDataDao
    }
}