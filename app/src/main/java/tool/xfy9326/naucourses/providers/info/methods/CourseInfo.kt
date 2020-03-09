package tool.xfy9326.naucourses.providers.info.methods

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.jwc.MyCourseScheduleTable
import tool.xfy9326.naucourses.providers.contents.methods.jwc.MyCourseScheduleTableNext
import tool.xfy9326.naucourses.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourses.providers.info.base.CacheExpire
import tool.xfy9326.naucourses.providers.info.base.CacheExpireRule
import tool.xfy9326.naucourses.providers.info.base.CacheExpireTimeUnit
import tool.xfy9326.naucourses.utils.utility.LogUtils

object CourseInfo : BaseSimpleContentInfo<CourseSet, CourseInfo.OperationType>() {
    private const val CACHE_EXPIRE_DAY = 1

    enum class OperationType {
        ASYNC_COURSE,
        INIT_DATA,
        THIS_TERM_COURSE,
        NEXT_TERM_COURSE
    }

    override fun onSaveSimpleResult(params: Set<OperationType>, data: CourseSet) {
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

    override fun loadSimpleStoredInfo(): CourseSet? = CoursesDBHelper.readCourseSet()

    override suspend fun getSimpleInfoContent(params: Set<OperationType>): ContentResult<CourseSet> {
        if (params.size != 1) {
            throw IllegalArgumentException("Params Amount Error!")
        } else {
            return when (params.first()) {
                OperationType.ASYNC_COURSE -> {
                    val result = MyCourseScheduleTable.getContentData()
                    if (result.isSuccess) {
                        val cachedCourseSet = getSimpleCachedItem()
                        val contentData = result.contentData!!
                        if (cachedCourseSet != null) {
                            val newSet = cachedCourseSet.copy()
                            if (newSet.update(contentData)) {
                                ContentResult(true, contentData = newSet)
                            } else {
                                LogUtils.d<CourseInfo>("Course Update Failed!")
                                ContentResult(false, ContentErrorReason.OPERATION)
                            }
                        } else {
                            val conflictCheck = CourseSet.checkCourseTimeConflict(contentData.courses)
                            if (conflictCheck.isSuccess) {
                                ContentResult(true, contentData = contentData)
                            } else {
                                LogUtils.d<CourseInfo>("Async New Courses Has Conflicts!\n${conflictCheck.printText()}")
                                ContentResult(false, ContentErrorReason.OPERATION)
                            }
                        }
                    } else {
                        result
                    }
                }
                OperationType.INIT_DATA, OperationType.THIS_TERM_COURSE -> {
                    val result = MyCourseScheduleTable.getContentData()
                    if (result.isSuccess) {
                        val conflictCheck = CourseSet.checkCourseTimeConflict(result.contentData!!.courses)
                        if (conflictCheck.isSuccess) {
                            result
                        } else {
                            LogUtils.d<CourseInfo>("Init or This Term Courses Has Conflicts!\n${conflictCheck.printText()}")
                            ContentResult(false, ContentErrorReason.OPERATION)
                        }
                    } else {
                        result
                    }
                }
                OperationType.NEXT_TERM_COURSE -> {
                    val result = MyCourseScheduleTableNext.getContentData()
                    if (result.isSuccess) {
                        val conflictCheck = CourseSet.checkCourseTimeConflict(result.contentData!!.courses)
                        if (conflictCheck.isSuccess) {
                            result
                        } else {
                            LogUtils.d<CourseInfo>("Next Term Courses Has Conflicts!\n${conflictCheck.printText()}")
                            ContentResult(false, ContentErrorReason.OPERATION)
                        }
                    } else {
                        result
                    }
                }
            }
        }
    }

    suspend fun saveNewCourses(courseSet: CourseSet) = withContext(Dispatchers.Default) {
        saveSimpleInfo(courseSet)
        updateSimpleCache(courseSet)
    }

    suspend fun updateCourses(courseSet: Set<Course>) = withContext(Dispatchers.Default) {
        updateSimpleCache(CoursesDBHelper.updateCourses(courseSet)!!)
    }

    override fun saveSimpleInfo(info: CourseSet) = CoursesDBHelper.storeNewCourseSet(info)

    override fun clearSimpleStoredInfo() = CoursesDBHelper.clearAllCoursesInfo()
}