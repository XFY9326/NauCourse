package tool.xfy9326.naucourse.providers.info.methods

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.db.CourseSetDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyCourseScheduleTable
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyCourseScheduleTableNext
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourse.providers.info.base.CacheExpire
import tool.xfy9326.naucourse.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourse.providers.info.base.CacheExpireTimeUnit
import tool.xfy9326.naucourse.utils.debug.LogUtils

object CourseInfo : BaseSimpleContentInfo<CourseSet, CourseInfo.OperationType>() {
    private const val CACHE_EXPIRE_DAY = 1

    enum class OperationType {
        ASYNC_COURSE,
        INIT_DATA,
        THIS_TERM_COURSE,
        NEXT_TERM_COURSE
    }

    override suspend fun onSaveSimpleResult(params: Set<OperationType>, data: CourseSet) {
        if (OperationType.ASYNC_COURSE in params || OperationType.INIT_DATA in params) {
            super.onSaveSimpleResult(params, data)
        }
    }

    override fun onSaveSimpleCache(params: Set<OperationType>, data: CourseSet) {
        if (OperationType.ASYNC_COURSE in params || OperationType.INIT_DATA in params) {
            super.onSaveSimpleCache(params, data)
        }
    }

    override fun isSimpleCacheExpired(params: Set<OperationType>, cacheExpire: CacheExpire): Boolean =
        when {
            OperationType.ASYNC_COURSE in params -> super.isSimpleCacheExpired(params, cacheExpire)
            OperationType.INIT_DATA in params -> true
            else -> true
        }

    override fun onGetCacheExpire(): CacheExpire = CacheExpire(CacheExpireRule.PER_TIME, CACHE_EXPIRE_DAY, CacheExpireTimeUnit.DAY)

    override suspend fun loadSimpleStoredInfo(): CourseSet? = CourseSetDBHelper.readCourseSet()

    override suspend fun getSimpleInfoContent(params: Set<OperationType>): ContentResult<CourseSet> {
        if (params.size != 1) {
            throw IllegalArgumentException("Params Amount Error! Params: $params")
        } else {
            return when (params.first()) {
                OperationType.INIT_DATA, OperationType.ASYNC_COURSE -> {
                    LogUtils.d<CourseInfo>("Starting Async/Init Courses Now: ${params.first()}")
                    val result = MyCourseScheduleTable.getContentData()
                    if (result.isSuccess) {
                        val cachedCourseSet = getSimpleCachedItem()
                        val contentData = result.contentData!!
                        if (cachedCourseSet != null) {
                            LogUtils.d<CourseInfo>("Updating Courses")
                            val newSet = cachedCourseSet.copy()
                            if (newSet.update(contentData)) {
                                ContentResult(true, contentData = newSet)
                            } else {
                                LogUtils.d<CourseInfo>("Course Update Failed!")
                                ContentResult(false, ContentErrorReason.OPERATION)
                            }
                        } else {
                            LogUtils.d<CourseInfo>("Settings New Courses")
                            val conflictCheck = CourseSet.checkCourseTimeConflict(contentData.courses)
                            if (conflictCheck.isSuccess) {
                                result
                            } else {
                                LogUtils.d<CourseInfo>("Async/Init New Courses Has Conflicts!\n${conflictCheck.printText()}")
                                ContentResult(false, ContentErrorReason.DATA_ERROR)
                            }
                        }
                    } else {
                        result
                    }
                }
                OperationType.THIS_TERM_COURSE -> MyCourseScheduleTable.getContentData()
                OperationType.NEXT_TERM_COURSE -> MyCourseScheduleTableNext.getContentData()
            }
        }
    }

    suspend fun saveNewCourses(courseSet: CourseSet) = withContext(Dispatchers.Default) {
        saveSimpleInfo(courseSet)
        updateSimpleCache(courseSet)
    }

    override suspend fun saveSimpleInfo(info: CourseSet) = CourseSetDBHelper.storeNewCourseSet(info)

    override suspend fun clearSimpleStoredInfo() = CourseSetDBHelper.clearAll()
}