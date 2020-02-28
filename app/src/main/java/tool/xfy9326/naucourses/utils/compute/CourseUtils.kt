package tool.xfy9326.naucourses.utils.compute

import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.beans.CourseCell
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.beans.CourseTimeDuration
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime
import java.util.*
import kotlin.collections.ArrayList

object CourseUtils {
    private const val NEXT_COURSE_BEFORE_COURSE_END_BASED_MINUTE = 10

    fun getCourseTableByWeekNum(courseSet: CourseSet, weekNum: Int, maxWeekNum: Int, startWeekDayNum: Int, endWeekDayNum: Int): CourseTable {
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
                    if (thisWeekCourse || temp[time.weekDay - 1][it.start - 1] == null) {
                        temp[time.weekDay - 1][it.start - 1] = CourseCell(
                            course.id,
                            course.name,
                            time.location,
                            time.weekDay,
                            weekNum,
                            CourseTimeDuration.parseTimePeriod(it),
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