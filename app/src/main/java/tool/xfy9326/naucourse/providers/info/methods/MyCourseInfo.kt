package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.CourseScoreDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyCourse
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit

object MyCourseInfo : BaseSimpleContentInfo<Array<CourseScore>, Nothing>() {
    private const val CACHE_EXPIRE_DAY = 1

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override suspend fun loadSimpleStoredInfo(): Array<CourseScore> = CourseScoreDBHelper.getCourseScores()

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Array<CourseScore>> = MyCourse.getContentData()

    override suspend fun saveSimpleInfo(info: Array<CourseScore>) = CourseScoreDBHelper.putCourseScores(info)

    override suspend fun clearSimpleStoredInfo() = CourseScoreDBHelper.clearAll()
}