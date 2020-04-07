package tool.xfy9326.naucourse.utils.courses

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.CourseCell
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.CourseItem
import tool.xfy9326.naucourse.beans.CourseTable
import tool.xfy9326.naucourse.io.store.CourseTableStore
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import java.util.*
import kotlin.collections.ArrayList

object CourseUtils {
    private const val NEXT_COURSE_BEFORE_COURSE_END_BASED_MINUTE = 10
    private const val CUSTOM_COURSE_ID_PREFIX = "NCC-"

    fun importCourseToList(
        currentList: ArrayList<Pair<Course, CourseCellStyle>>,
        importedCourses: ArrayList<Course>
    ): ArrayList<Pair<Course, CourseCellStyle>> {
        var hasSame: Boolean
        val newCourses = ArrayList(currentList)
        for (newCourse in importedCourses) {
            hasSame = false
            for (coursePair in currentList) {
                if (newCourse.id == coursePair.first.id) {
                    newCourses.remove(coursePair)
                    newCourses.add(Pair(newCourse, coursePair.second))
                    hasSame = true
                    break
                }
            }

            if (!hasSame) {
                newCourses.add(Pair(newCourse, CourseStyleUtils.getDefaultCellStyle(newCourse.id)))
            }
        }
        return newCourses
    }

    private fun getCourseTableByWeekNum(
        courseSet: CourseSet,
        weekNum: Int,
        maxWeekNum: Int,
        startWeekDayNum: Int,
        endWeekDayNum: Int
    ):
            CourseTable {
        if (weekNum < Constants.Course.MIN_WEEK_NUM_SIZE || weekNum > maxWeekNum) {
            throw IllegalArgumentException("Week Num Error! Num: $weekNum")
        }
        val courses = courseSet.courses
        val temp = Array<Array<CourseCell?>>(Constants.Time.MAX_WEEK_DAY) {
            arrayOfNulls(Constants.Course.MAX_COURSE_LENGTH)
        }
        for (course in courses) {
            for (time in course.timeSet) {
                val thisWeekCourse = time.isWeekNumTrue(weekNum)
                time.coursesNumArray.timePeriods.forEach {
                    if (thisWeekCourse || temp[time.weekDay - 1][it.start - 1] == null || temp[time.weekDay - 1][it.start - 1]!!.courseTime < time) {
                        temp[time.weekDay - 1][it.start - 1] = CourseCell(
                            course.id,
                            course.name,
                            time,
                            weekNum,
                            TimeUtils.parseTimePeriod(it),
                            thisWeekCourse
                        )
                    }
                }
            }
        }
        val courseTable = ArrayList<Array<CourseCell>>(Constants.Time.MAX_WEEK_DAY)
        var startP = 0
        var endP = temp.size - 1
        if (weekNum == Constants.Course.MIN_WEEK_NUM_SIZE) {
            startP = startWeekDayNum - 1
        } else if (weekNum == maxWeekNum) {
            endP = endWeekDayNum - 1
        }
        for ((i, weekDayCourse) in temp.withIndex()) {
            if (i < startP || i > endP) {
                courseTable.add(emptyArray())
            } else {
                val coursesArr = ArrayList<CourseCell>()
                for (cell in weekDayCourse) {
                    if (cell != null) coursesArr.add(cell)
                }
                courseTable.add(coursesArr.toTypedArray())
            }
        }
        return CourseTable(courseTable.toTypedArray())
    }

    fun getTomorrowCourse(courseSet: CourseSet, termDate: TermDate): Array<CourseItem> {
        val calendarNow = Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = System.currentTimeMillis()
        }
        calendarNow.add(Calendar.DATE, 1)
        return getCourseInDay(courseSet, termDate, calendarNow)
    }

    fun getTodayCourse(courseSet: CourseSet, termDate: TermDate): Array<CourseItem> {
        val calendarNow = Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = System.currentTimeMillis()
        }
        return getCourseInDay(courseSet, termDate, calendarNow)
    }

    private fun getCourseInDay(courseSet: CourseSet, termDate: TermDate, calendarNow: Calendar): Array<CourseItem> {
        termDate.refreshCurrentWeekNum()
        return if (termDate.inVacation) {
            emptyArray()
        } else {
            val nowWeekDayNum = TimeUtils.getWeekDayNum(calendarNow)
            val courses = courseSet.courses
            val courseTable = ArrayList<CourseItem>()
            for (course in courses) {
                for (time in course.timeSet) {
                    if (time.weekDay.toInt() == nowWeekDayNum && time.isWeekNumTrue(termDate.currentWeekNum)) {
                        time.coursesNumArray.timePeriods.forEach {
                            courseTable.add(
                                CourseItem(
                                    course, time, it, TimeUtils.getCourseDateTimePeriod(calendarNow.time, it), nowWeekDayNum.toShort()
                                )
                            )
                        }
                    }
                }
            }
            courseTable.sortedBy {
                it.dateTimePeriod.startDateTime
            }.toTypedArray()
        }
    }

    fun getNotThisWeekCourse(courseSet: CourseSet, termDate: TermDate): Array<Pair<Course, CourseTime>> {
        termDate.refreshCurrentWeekNum()
        return if (termDate.inVacation) {
            emptyArray()
        } else {
            val courses = courseSet.courses
            val courseTable = ArrayList<Pair<Course, CourseTime>>()
            for (course in courses) {
                for (time in course.timeSet) {
                    if (!time.isWeekNumTrue(termDate.currentWeekNum)) {
                        time.coursesNumArray.timePeriods.forEach { _ ->
                            courseTable.add(Pair(course, time))
                        }
                    }
                }
            }
            courseTable.sortedBy {
                it.first.id
            }.toTypedArray()
        }
    }

    fun getTodayNextCoursePosition(todayCourse: Array<CourseItem>): Int? {
        val timeOffset = NEXT_COURSE_BEFORE_COURSE_END_BASED_MINUTE * 60 * 1000
        var position = 0

        val nowTime = Date()

        for (courseItem in todayCourse) {
            if (courseItem.dateTimePeriod.endDateTime.time - timeOffset >= nowTime.time) {
                break
            }
            position++
        }
        return if (position == todayCourse.size) {
            null
        } else {
            position
        }
    }

    suspend fun generateAllCourseTable(
        courseSet: CourseSet,
        termDate: TermDate,
        maxWeekNum: Int
    ): Array<CourseTable> =
        coroutineScope {
            val result = arrayOfNulls<CourseTable>(maxWeekNum)
            val waitArr = arrayOfNulls<Deferred<CourseTable>>(maxWeekNum)

            val startWeekDayNum = TimeUtils.getWeekDayNum(termDate.startDate)
            val endWeekDayNum = TimeUtils.getWeekDayNum(termDate.endDate)
            for (i in 0 until maxWeekNum) {
                waitArr[i] = async {
                    getCourseTableByWeekNum(courseSet, i + 1, maxWeekNum, startWeekDayNum, endWeekDayNum)
                }
            }
            for (i in 0 until maxWeekNum) {
                result[i] = waitArr[i]?.await()
            }
            val output = result.requireNoNulls()
            launch { CourseTableStore.saveStore(output) }
            output
        }

    fun getNewCourseId() = CUSTOM_COURSE_ID_PREFIX + (System.currentTimeMillis() / 1000)

    fun hasWeekendCourse(courseTable: CourseTable): Boolean =
        courseTable.table.isNotEmpty() &&
                (courseTable.table[Constants.Time.MAX_WEEK_DAY - 1].isNotEmpty() || courseTable.table[Constants.Time.MAX_WEEK_DAY - 2].isNotEmpty())
}