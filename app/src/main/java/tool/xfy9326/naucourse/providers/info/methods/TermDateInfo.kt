package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.TermDateDBHelper
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.TermInfo
import tool.xfy9326.naucourse.providers.info.base.*
import tool.xfy9326.naucourse.utils.courses.TimeUtils

object TermDateInfo : BaseSimpleContentInfo<TermDate, TermDateInfo.TermType>() {
    private const val CACHE_EXPIRE_DAY = 1

    enum class TermType {
        RAW_TERM
    }

    override suspend fun loadSimpleStoredInfo(): TermDate? = TermDateDBHelper.getTermDate()

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override suspend fun getSimpleInfoContent(params: Set<TermType>): ContentResult<TermDate> {
        val result = TermInfo.getContentData()
        return if (result.isSuccess) {
            ContentResult(true, contentData = fixTermDate(result.contentData!!))
        } else {
            result
        }
    }

    @Synchronized
    override suspend fun getInfo(params: Set<TermType>, loadCache: Boolean, forceRefresh: Boolean): InfoResult<TermDate> {
        val termDateResult = super.getInfo(params, loadCache, forceRefresh)
        return if (forceRefresh) {
            termDateResult
        } else {
            val customTermDate = AppPref.readSavedCustomTermDate()
            if (customTermDate == null || TermType.RAW_TERM in params) {
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

    fun saveCustomTermDate(info: TermDate) = AppPref.saveCustomTermDate(info)

    fun clearCustomTermDate() = AppPref.clearCustomTermDate()

    override fun onReadSimpleCache(data: TermDate): TermDate =
        fixTermDate(data)

    override suspend fun saveSimpleInfo(info: TermDate) = TermDateDBHelper.putTermDate(info)

    override suspend fun clearSimpleStoredInfo() = TermDateDBHelper.clearAll()

    private fun fixTermDate(termDate: TermDate): TermDate = TermDate(TimeUtils.getWeekNum(termDate), termDate.startDate, termDate.endDate)
}