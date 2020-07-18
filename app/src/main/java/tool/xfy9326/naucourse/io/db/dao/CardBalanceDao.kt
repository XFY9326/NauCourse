package tool.xfy9326.naucourse.io.db.dao

import androidx.room.*
import tool.xfy9326.naucourse.constants.DBConst
import tool.xfy9326.naucourse.io.db.CardBalanceDBHelper
import tool.xfy9326.naucourse.providers.beans.ykt.CardBalance

@Dao
interface CardBalanceDao {
    @Transaction
    fun setCardBalance(cardBalance: CardBalance) {
        clearCardBalance()
        clearIndex()
        putCardBalance(cardBalance)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCardBalance(vararg cardBalance: CardBalance)

    @Query("select * from ${CardBalanceDBHelper.CARD_BALANCE_TABLE_NAME} limit 1")
    fun getCardBalance(): Array<CardBalance>

    @Query("delete from ${CardBalanceDBHelper.CARD_BALANCE_TABLE_NAME}")
    fun clearCardBalance()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${DBConst.SQL_LITE_TABLE} where ${DBConst.COLUMN_NAME} = '${CardBalanceDBHelper.CARD_BALANCE_TABLE_NAME}'")
    fun clearIndex()
}