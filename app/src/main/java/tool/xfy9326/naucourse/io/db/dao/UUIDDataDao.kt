package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.beans.UUIDContent
import tool.xfy9326.naucourse.io.db.UUIDDBHelper

@Dao
interface UUIDDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putUUID(vararg uuidContent: UUIDContent)

    @Query("select * from ${UUIDDBHelper.UUID_TABLE_NAME}")
    fun getUUID(): Array<UUIDContent>

    @Query("delete from ${UUIDDBHelper.UUID_TABLE_NAME}")
    fun clearUUID()
}