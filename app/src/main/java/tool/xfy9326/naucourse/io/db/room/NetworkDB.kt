package tool.xfy9326.naucourse.io.db.room

import androidx.room.Database
import tool.xfy9326.naucourse.io.db.base.BaseDB
import tool.xfy9326.naucourse.io.db.dao.NGXCookieDataDao
import tool.xfy9326.naucourse.io.db.dao.SSOCookieDataDao
import tool.xfy9326.naucourse.network.beans.NGXCookieData
import tool.xfy9326.naucourse.network.beans.SSOCookieData

object NetworkDB : BaseDB<NetworkDB.NetworkDataBase>() {
    override val dbName: String = "Network.db"
    override val dbClass: Class<NetworkDataBase> = NetworkDataBase::class.java

    private const val NETWORK_DB_VERSION = 1

    @Database(
        entities = [SSOCookieData::class, NGXCookieData::class],
        version = NETWORK_DB_VERSION
    )
    abstract class NetworkDataBase : DB() {
        abstract fun getSSOCookiesDataDao(): SSOCookieDataDao

        abstract fun getNGXCookiesDataDao(): NGXCookieDataDao
    }
}