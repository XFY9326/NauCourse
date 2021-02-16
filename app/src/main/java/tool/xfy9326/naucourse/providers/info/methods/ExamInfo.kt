package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.ExamDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.Exam
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyExamArrangeList
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit

object ExamInfo : BaseSimpleContentInfo<Array<Exam>, Nothing>() {
    private const val CACHE_EXPIRE_HOUR = 1

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_HOUR, CacheExpireTimeUnit.HOUR
    )

    override suspend fun loadSimpleStoredInfo(): Array<Exam> = ExamDBHelper.getExam()

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Array<Exam>> = MyExamArrangeList.getContentData()

    override suspend fun saveSimpleInfo(info: Array<Exam>) = ExamDBHelper.putExam(info)

    override suspend fun clearSimpleStoredInfo() = ExamDBHelper.clearAll()
}