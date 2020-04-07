package tool.xfy9326.naucourse.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.io.db.UUIDDBHelper

@Entity(tableName = UUIDDBHelper.UUID_TABLE_NAME)
data class UUIDContent(
    @PrimaryKey
    val uuid: String
)