package tool.xfy9326.naucourse.io.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tool.xfy9326.naucourse.io.db.base.BaseDB
import tool.xfy9326.naucourse.io.db.dao.SSOCookieDataDao
import tool.xfy9326.naucourse.network.beans.SSOCookieData

object NetworkDB : BaseDB<NetworkDB.NetworkDataBase>() {
    override val dbName: String = "Network.db"
    override val dbClass: Class<NetworkDataBase> = NetworkDataBase::class.java

    private const val NETWORK_DB_VERSION = 2

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE NGXCookies")
        }
    }

    override fun onBuildDB(builder: RoomDatabase.Builder<NetworkDataBase>): NetworkDataBase {
        builder.addMigrations(MIGRATION_1_2)
        return super.onBuildDB(builder)
    }

    @Database(
        entities = [SSOCookieData::class],
        version = NETWORK_DB_VERSION
    )
    abstract class NetworkDataBase : DB() {
        abstract fun getSSOCookiesDataDao(): SSOCookieDataDao
    }
}