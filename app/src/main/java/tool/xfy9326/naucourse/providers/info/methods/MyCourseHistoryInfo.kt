package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.CourseHistoryDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyCourseHistory
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit

object MyCourseHistoryInfo : BaseSimpleContentInfo<Array<CourseHistory>, Nothing>() {
    private const val CACHE_EXPIRE_DAY = 1

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override suspend fun loadSimpleStoredInfo(): Array<CourseHistory>? = CourseHistoryDBHelper.getCourseHistoryArr()

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Array<CourseHistory>> = MyCourseHistory.getContentData()

    override suspend fun saveSimpleInfo(info: Array<CourseHistory>) = CourseHistoryDBHelper.putCourseHistoryArr(info)

    override suspend fun clearSimpleStoredInfo() = CourseHistoryDBHelper.clearAll()
}