package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriod
import java.io.Serializable

// 课程数据
data class CourseItem(
    // 课程
    val course: Course,
    // 课程时间
    val courseTime: CourseTime,
    // 当且仅当显示的是非本周课程时，该值为空
    val detail: Detail?
) : Serializable {

    constructor(course: Course, courseTime: CourseTime) : this(course, courseTime, null)

    constructor(course: Course, courseTime: CourseTime, timePeriod: TimePeriod, dateTimePeriod: DateTimePeriod, weekDayNum: Short) :
            this(course, courseTime, Detail(timePeriod, dateTimePeriod, weekDayNum))

    data class Detail(
        val timePeriod: TimePeriod,
        val dateTimePeriod: DateTimePeriod,
        val weekDayNum: Short
    ) : Serializable
}