package tool.xfy9326.naucourses.providers.info.methods

import tool.xfy9326.naucourses.beans.CardBalance
import tool.xfy9326.naucourses.io.gson.GsonStoreManager
import tool.xfy9326.naucourses.io.gson.GsonStoreType
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.ykt.StudentCardBalance
import tool.xfy9326.naucourses.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourses.providers.info.base.CacheExpire
import tool.xfy9326.naucourses.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourses.providers.info.base.CacheExpireTimeUnit

object CardBalanceInfo : BaseSimpleContentInfo<CardBalance, Nothing>() {
    private const val CACHE_EXPIRE_MINUTE = 5

    override fun loadSimpleStoredInfo(): CardBalance? = GsonStoreManager.readData(GsonStoreType.CARD_BALANCE, true)

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(CacheExpireRule.PER_TIME, CACHE_EXPIRE_MINUTE, CacheExpireTimeUnit.MINUTE)

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<CardBalance> = StudentCardBalance.getContentData()

    override fun saveSimpleInfo(info: CardBalance) {
        GsonStoreManager.writeData(GsonStoreType.CARD_BALANCE, info, true)
    }

    override fun clearSimpleStoredInfo() {
        GsonStoreManager.clearData(GsonStoreType.CARD_BALANCE)
    }
}