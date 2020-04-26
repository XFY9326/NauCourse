package tool.xfy9326.naucourse.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.io.db.UUIDDBHelper

// UUID存储，该类用于数据库存储
@Entity(tableName = UUIDDBHelper.UUID_TABLE_NAME)
data class UUIDContent(
    @PrimaryKey
    val uuid: String
)