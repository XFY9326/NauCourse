package tool.xfy9326.naucourses.providers.info.methods

import tool.xfy9326.naucourses.io.gson.GsonStoreManager
import tool.xfy9326.naucourses.io.gson.GsonStoreType
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.jwc.StudentIndex
import tool.xfy9326.naucourses.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourses.providers.info.base.CacheExpire
import tool.xfy9326.naucourses.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourses.providers.info.base.CacheExpireTimeUnit

object PersonalInfo : BaseSimpleContentInfo<StudentInfo, Nothing>() {
    private const val CACHE_EXPIRE_DAY = 1

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(
        CacheExpireRule.PER_TIME,
        CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY
    )

    override fun loadSimpleStoredInfo(): StudentInfo? =
        GsonStoreManager.readData(GsonStoreType.STUDENT_INFO, true)

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<StudentInfo> = StudentIndex.getContentData()

    override fun saveSimpleInfo(info: StudentInfo) {
        GsonStoreManager.writeData(GsonStoreType.STUDENT_INFO, info, true)
    }

    override fun clearSimpleStoredInfo() {
        GsonStoreManager.clearData(GsonStoreType.STUDENT_INFO)
    }
}