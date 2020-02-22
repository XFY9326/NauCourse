package tool.xfy9326.naucourses.utils.compute

import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.beans.CourseCell
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.beans.CourseTimeDuration
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import java.util.*
import kotlin.collections.ArrayList

object CourseUtils {
    private const val NEXT_COURSE_BEFORE_COURSE_END_BASED_MINUTE = 10

    fun getCourseTableByWeekNum(courseSet: CourseSet, weekNum: Int): CourseTable {
        if (weekNum < Constants.Course.MIN_WEEK_NUM_SIZE || weekNum > Constants.Course.MAX_WEEK_NUM_SIZE) {
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
                    if (thisWeekCourse || temp[time.weekDay - 1][it.start - 1] == null) {
                        temp[time.weekDay - 1][it.start - 1] = CourseCell(
                            course.name,
                            time.location,
                            course.id,
                            CourseTimeDuration.parseTimePeriod(it),
                            thisWeekCourse
                        )
                    }
                }
            }
        }
        val courseTable = ArrayList<Array<CourseCell>>(Constants.Time.MAX_WEEK_DAY)
        for (weekDayCourse in temp) {
            val coursesArr = ArrayList<CourseCell>()
            for (cell in weekDayCourse) {
                if (cell != null) coursesArr.add(cell)
            }
            courseTable.add(coursesArr.toTypedArray())
        }
        return CourseTable(courseTable.toTypedArray())
    }

    fun getTodayCourse(courseSet: CourseSet, weekNum: Int, nowTime: Date = Date()): Array<Triple<Course, CourseTime, CourseTimeDuration>> {
        val calendarNow = Calendar.getInstance(Locale.CHINA).apply {
            time = nowTime
        }
        val nowWeekDayNum = TimeUtils.getWeekDayNum(calendarNow)
        val courses = courseSet.courses
        val courseTable = ArrayList<Triple<Course, CourseTime, CourseTimeDuration>>()
        for (course in courses) {
            for (time in course.timeSet) {
                if (time.weekDay.toInt() == nowWeekDayNum && time.isWeekNumTrue(weekNum)) {
                    time.coursesNumArray.timePeriods.forEach {
                        courseTable.add(Triple(course, time, CourseTimeDuration.parseTimePeriod(it)))
                    }
                }
            }
        }
        return courseTable.toTypedArray()
    }

    @Synchronized
    fun asyncCellStyle(courseSet: CourseSet, styleMap: Array<CourseCellStyle>? = null, saveStyle: Boolean = true): Array<CourseCellStyle> {
        val oldStyles = styleMap ?: emptyArray()
        val newStyles = ArrayList<CourseCellStyle>(courseSet.courses.size)
        for (course in courseSet.courses) {
            newStyles.add(CourseCellStyle.getStyleByCourseId(course.id, oldStyles) ?: CourseCellStyle.getDefaultCellStyle(course.id))
        }
        val result = newStyles.toTypedArray()
        if (saveStyle) {
            CourseCellStyleStore.saveStore(result)
        }
        return result
    }

    fun getTodayNextCourse(
        todayCourse: Array<Triple<Course, CourseTime, CourseTimeDuration>>,
        nowTime: Date = Date()
    ): Int? {
        val timeOffset = NEXT_COURSE_BEFORE_COURSE_END_BASED_MINUTE * 60 * 1000
        var position = 0

        for (entry in todayCourse) {
            val dateTimePeriod = TimeUtils.getCourseDateTimePeriod(
                nowTime,
                CourseTimeDuration.convertToTimePeriod(entry.third)
            )
            if (dateTimePeriod.endDateTime.time - timeOffset >= nowTime.time) {
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
}