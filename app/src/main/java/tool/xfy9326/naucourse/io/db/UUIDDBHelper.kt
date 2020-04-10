package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.beans.UUIDContent
import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.AppDB

object UUIDDBHelper : BaseDBHelper<AppDB.AppDataBase>() {
    const val UUID_TABLE_NAME = "UUID"

    override val db: AppDB.AppDataBase = AppDB.getDB()

    @Synchronized
    fun saveUUID(newUUID: String) = with(db.getUUIDDataDao()) {
        clearAll()
        putUUID(UUIDContent(newUUID))
    }

    @Synchronized
    fun readUUID() = with(db.getUUIDDataDao()) {
        val uuidArr = getUUID()
        if (uuidArr.isNullOrEmpty()) {
            null
        } else {
            uuidArr[0].uuid
        }
    }

    @Synchronized
    override fun clearAll() = with(db.getUUIDDataDao()) {
        clearUUID()
    }
}