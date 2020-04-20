package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.store.SuspendCourseStore
import tool.xfy9326.naucourse.io.store.base.BaseJsonStore
import tool.xfy9326.naucourse.providers.beans.jwc.SuspendCourse
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.SuspendCourseInfo
import tool.xfy9326.naucourse.providers.info.base.BaseJsonStoreInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit

object SuspendCourseInfo : BaseJsonStoreInfo<Array<SuspendCourse>, Nothing>() {
    private const val CACHE_EXPIRE_HOUR = 1
    override val jsonStore: BaseJsonStore<Array<SuspendCourse>> = SuspendCourseStore

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_HOUR, CacheExpireTimeUnit.HOUR
    )

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Array<SuspendCourse>> = SuspendCourseInfo.getContentData()
}