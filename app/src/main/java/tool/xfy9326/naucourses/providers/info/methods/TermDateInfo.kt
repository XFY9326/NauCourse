package tool.xfy9326.naucourses.providers.info.methods

import tool.xfy9326.naucourses.io.dbHelpers.AppDBHelper
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.jwc.TermInfo
import tool.xfy9326.naucourses.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourses.providers.info.base.CacheExpire
import tool.xfy9326.naucourses.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourses.providers.info.base.CacheExpireTimeUnit
import tool.xfy9326.naucourses.utils.compute.TimeUtils

object TermDateInfo : BaseSimpleContentInfo<TermDate, Nothing>() {
    private const val CACHE_EXPIRE_DAY = 1

    override fun loadSimpleStoredInfo(): TermDate? = AppDBHelper.getTermDate()

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<TermDate> {
        val result = TermInfo.getContentData()
        return if (result.isSuccess) {
            ContentResult(true, contentData = fixTermDate(result.contentData!!))
        } else {
            result
        }
    }

    override fun onReadSimpleCache(data: TermDate): TermDate =
        fixTermDate(data)

    override fun saveSimpleInfo(info: TermDate) = AppDBHelper.putTermDate(info)

    override fun clearSimpleStoredInfo() = AppDBHelper.clearTermDate()

    private fun fixTermDate(termDate: TermDate): TermDate = TermDate(TimeUtils.getWeekNum(termDate), termDate.startDate, termDate.endDate)
}