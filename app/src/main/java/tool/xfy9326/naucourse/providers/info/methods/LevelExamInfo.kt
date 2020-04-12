package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.LevelExamDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyLevelExam
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit

object LevelExamInfo : BaseSimpleContentInfo<Array<LevelExam>, Nothing>() {
    private const val CACHE_EXPIRE_DAY = 1

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override fun loadSimpleStoredInfo(): Array<LevelExam>? = LevelExamDBHelper.getLevelExam()

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Array<LevelExam>> = MyLevelExam.getContentData()

    override fun saveSimpleInfo(info: Array<LevelExam>) = LevelExamDBHelper.putLevelExam(info)

    override fun clearSimpleStoredInfo() = LevelExamDBHelper.clearAll()
}