package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.providers.beans.ykt.CardBalance
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.ykt.StudentCardBalance
import tool.xfy9326.naucourse.providers.info.base.BaseJsonStoreInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit
import tool.xfy9326.naucourse.providers.store.CardBalanceStore
import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore
import tool.xfy9326.naucourse.utils.debug.LogUtils

object CardBalanceInfo : BaseJsonStoreInfo<CardBalance, Nothing>() {
    private const val CACHE_EXPIRE_MINUTE = 5
    override val jsonStore: BaseJsonStore<CardBalance> = CardBalanceStore

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(CacheExpireRule.PER_TIME, CACHE_EXPIRE_MINUTE, CacheExpireTimeUnit.MINUTE)

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<CardBalance> {
        LogUtils.d<CardBalanceInfo>("Getting New Card Balance Now")
        return StudentCardBalance.getContentData()
    }
}