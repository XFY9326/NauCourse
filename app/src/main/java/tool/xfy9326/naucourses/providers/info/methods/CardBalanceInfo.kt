package tool.xfy9326.naucourses.providers.info.methods

import tool.xfy9326.naucourses.beans.CardBalance
import tool.xfy9326.naucourses.io.gson.GsonStoreManager
import tool.xfy9326.naucourses.io.gson.GsonStoreType
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.ykt.StudentCardBalance
import tool.xfy9326.naucourses.providers.info.base.BaseSimpleContentInfo

object CardBalanceInfo : BaseSimpleContentInfo<CardBalance, Nothing>() {
    override fun loadSimpleStoredInfo(): CardBalance? = GsonStoreManager.readData(GsonStoreType.CARD_BALANCE, true)

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<CardBalance> = StudentCardBalance.getContentData()

    override fun saveSimpleInfo(info: CardBalance) {
        GsonStoreManager.writeData(GsonStoreType.CARD_BALANCE, info, true)
    }

    override fun clearSimpleStoredInfo() {
        GsonStoreManager.clearData(GsonStoreType.CARD_BALANCE)
    }
}