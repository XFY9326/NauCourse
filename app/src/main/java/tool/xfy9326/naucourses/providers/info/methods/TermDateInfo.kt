package tool.xfy9326.naucourses.providers.info.methods

import tool.xfy9326.naucourses.io.dbHelpers.JwcDBHelper
import tool.xfy9326.naucourses.io.prefs.AppPref
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.jwc.TermInfo
import tool.xfy9326.naucourses.providers.info.base.*
import tool.xfy9326.naucourses.utils.compute.TimeUtils

object TermDateInfo : BaseSimpleContentInfo<TermDate, Nothing>() {
    private const val CACHE_EXPIRE_DAY = 1

    override fun loadSimpleStoredInfo(): TermDate? = JwcDBHelper.getTermDate()

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<TermDate> {
        val result = TermInfo.getContentData()
        return if (result.isSuccess) {
            ContentResult(true, contentData = fixTermDate(result.contentData!!))
        } else {
            result
        }
    }

    @Synchronized
    override suspend fun getInfo(params: Set<Nothing>, loadCache: Boolean, forceRefresh: Boolean): InfoResult<TermDate> {
        val termDateResult = super.getInfo(params, loadCache, forceRefresh)
        return if (forceRefresh) {
            termDateResult
        } else {
            val customTermDate = AppPref.readSavedCustomTermDate()
            if (customTermDate == null) {
                termDateResult
            } else {
                if (termDateResult.isSuccess) {
                    val termDate = termDateResult.data!!
                    // 自定义学期不被用于旧学期时间的设定，因此该设定被用于新学期或对当前学期的更正
                    if (termDate.getTerm() <= customTermDate.getTerm()) {
                        InfoResult(true, customTermDate)
                    } else {
                        termDateResult
                    }
                } else {
                    InfoResult(true, customTermDate)
                }
            }
        }
    }

    override fun onReadSimpleCache(data: TermDate): TermDate =
        fixTermDate(data)

    override fun saveSimpleInfo(info: TermDate) = JwcDBHelper.putTermDate(info)

    override fun clearSimpleStoredInfo() = JwcDBHelper.clearTermDate()

    private fun fixTermDate(termDate: TermDate): TermDate = TermDate(TimeUtils.getWeekNum(termDate), termDate.startDate, termDate.endDate)
}