package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.StudentIndex
import tool.xfy9326.naucourse.providers.info.base.BaseJsonStoreInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit
import tool.xfy9326.naucourse.providers.store.StudentInfoStore
import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore

object PersonalInfo : BaseJsonStoreInfo<StudentInfo, Nothing>() {
    private const val CACHE_EXPIRE_DAY = 1
    override val jsonStore: BaseJsonStore<StudentInfo> = StudentInfoStore

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<StudentInfo> = StudentIndex.getContentData()
}