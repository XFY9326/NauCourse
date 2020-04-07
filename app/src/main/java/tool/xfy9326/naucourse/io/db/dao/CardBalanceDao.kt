package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.CardBalanceDBHelper
import tool.xfy9326.naucourse.providers.beans.ykt.CardBalance

@Dao
interface CardBalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCardBalance(vararg cardBalance: CardBalance)

    @Query("select * from ${CardBalanceDBHelper.CARD_BALANCE_TABLE_NAME} limit 1")
    fun getCardBalance(): Array<CardBalance>

    @Query("delete from ${CardBalanceDBHelper.CARD_BALANCE_TABLE_NAME}")
    fun clearCardBalance()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '${CardBalanceDBHelper.CARD_BALANCE_TABLE_NAME}'")
    fun clearIndex()
}