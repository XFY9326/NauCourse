package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.beans.jwc.TimePeriod
import java.io.Serializable

data class CourseDetail(
    val course: Course,
    val termDate: TermDate,
    val courseLocation: String,
    val weekDayNum: Short,
    val weekNum: Int,
    val timePeriod: TimePeriod,
    val courseCellStyle: CourseCellStyle
) : Serializable