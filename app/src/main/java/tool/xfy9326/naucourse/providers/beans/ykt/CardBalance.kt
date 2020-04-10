package tool.xfy9326.naucourse.providers.beans.ykt

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.CardBalanceDBHelper
import java.util.*

@Entity(tableName = CardBalanceDBHelper.CARD_BALANCE_TABLE_NAME)
data class CardBalance(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
    val id: Int,
    val mainBalance: Float,
    val supportBalance: Float,
    val updateDate: Date
) {
    constructor(mainBalance: Float, supportBalance: Float) : this(Constants.DB.DEFAULT_ID, mainBalance, supportBalance, Date())
}