package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.AppDB
import tool.xfy9326.naucourse.providers.beans.ykt.CardBalance

object CardBalanceDBHelper : BaseDBHelper<AppDB.AppDataBase>() {
    const val CARD_BALANCE_TABLE_NAME = "CardBalance"

    override val db: AppDB.AppDataBase = AppDB.getDB()

    @Synchronized
    fun saveCardBalance(cardBalance: CardBalance) = db.getCardBalanceDao().setCardBalance(cardBalance)

    @Synchronized
    fun readCardBalance() = with(db.getCardBalanceDao()) {
        val data = getCardBalance()
        if (data.isNullOrEmpty()) {
            null
        } else {
            data[0]
        }
    }

    @Synchronized
    override fun clearAll() = with(db.getCardBalanceDao()) {
        clearCardBalance()
        clearIndex()
    }
}