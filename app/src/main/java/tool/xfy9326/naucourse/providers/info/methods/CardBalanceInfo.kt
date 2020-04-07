package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.CardBalanceDBHelper
import tool.xfy9326.naucourse.providers.beans.ykt.CardBalance
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.ykt.StudentCardBalance
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit
import tool.xfy9326.naucourse.utils.debug.LogUtils

object CardBalanceInfo : BaseSimpleContentInfo<CardBalance, Nothing>() {
    private const val CACHE_EXPIRE_MINUTE = 5

    override fun loadSimpleStoredInfo(): CardBalance? = CardBalanceDBHelper.readCardBalance()

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(CacheExpireRule.PER_TIME, CACHE_EXPIRE_MINUTE, CacheExpireTimeUnit.MINUTE)

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<CardBalance> {
        LogUtils.d<CardBalanceInfo>("Getting New Card Balance Now")
        return StudentCardBalance.getContentData()
    }

    override fun saveSimpleInfo(info: CardBalance) {
        CardBalanceDBHelper.saveCardBalance(info)
    }

    override fun clearSimpleStoredInfo() = CardBalanceDBHelper.clearAll()
}