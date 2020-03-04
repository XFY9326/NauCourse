package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourses.providers.beans.jwc.TimePeriod

data class CourseItem(
    val course: Course,
    val courseTime: CourseTime,
    val timePeriod: TimePeriod,
    val dateTimePeriod: DateTimePeriod,
    val weekDayNum: Short
)