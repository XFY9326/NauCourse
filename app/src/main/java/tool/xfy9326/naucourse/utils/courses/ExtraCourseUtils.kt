package tool.xfy9326.naucourse.utils.courses

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import tool.xfy9326.naucourse.beans.CourseBundle
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.info.methods.CourseInfo
import tool.xfy9326.naucourse.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils

object ExtraCourseUtils {
    suspend fun getLocalCourseData() = supervisorScope {
        val simpleCourseData = getLocalSimpleCourseData()
        var styleList: Array<CourseCellStyle>? = null

        if (simpleCourseData != null) {
            styleList = CourseCellStyleDBHelper.loadCourseCellStyle(simpleCourseData.first)
        }

        if (simpleCourseData != null && styleList != null) {
            return@supervisorScope Triple(simpleCourseData.first, simpleCourseData.second, styleList)
        }
        return@supervisorScope null
    }

    suspend fun getLocalSimpleCourseData() = supervisorScope {
        val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
        val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }

        val courseInfo = courseInfoAsync.await()
        val termDateInfo = termInfoAsync.await()

        var courseData: CourseSet? = null
        var termDateData: TermDate? = null

        if (termDateInfo.isSuccess) {
            termDateData = termDateInfo.data!!
        } else {
            LogUtils.d<AppWidgetUtils>("Term Date Get Error! Reason: ${termDateInfo.errorReason}")
        }

        if (termDateInfo.isSuccess && courseInfo.isSuccess) {
            courseData = courseInfo.data
        } else if (!courseInfo.isSuccess) {
            LogUtils.d<AppWidgetUtils>("Course Data Get Error! Reason: ${courseInfo.errorReason}")
        }

        if (courseData != null && termDateData != null) {
            termDateData.refreshCurrentWeekNum()
            return@supervisorScope Pair(courseData, termDateData)
        }
        return@supervisorScope null
    }

    fun getNextCourseInfo(courseSet: CourseSet, termDate: TermDate, courseBundle: CourseBundle? = null) =
        if (!courseSet.hasCourse) {
            NextCourseBundle()
        } else if (termDate.inVacation) {
            NextCourseBundle(true)
        } else {
            if (courseBundle == null) {
                NextCourseBundle(false)
            } else {
                NextCourseBundle(courseBundle)
            }
        }

    suspend fun getNextCourseInfo(courseSet: CourseSet, termDate: TermDate, styleList: Array<CourseCellStyle>) =
        supervisorScope {
            return@supervisorScope if (!courseSet.hasCourse) {
                NextCourseBundle()
            } else if (termDate.inVacation) {
                NextCourseBundle(true)
            } else {
                val nextCourseItem = CourseUtils.getTodayNextCourse(courseSet, termDate)
                if (nextCourseItem == null) {
                    NextCourseBundle(false)
                } else {
                    NextCourseBundle(CourseBundle(nextCourseItem, CourseStyleUtils.getStyleByCourseId(nextCourseItem.course.id, styleList)!!))
                }
            }
        }
}