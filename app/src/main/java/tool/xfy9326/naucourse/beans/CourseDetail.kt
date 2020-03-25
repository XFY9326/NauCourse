package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriod
import java.io.Serializable

data class CourseDetail(
    val course: Course,
    val termDate: TermDate,
    val courseCellStyle: CourseCellStyle,
    val timeDetail: TimeDetail? = null
) : Serializable {
    data class TimeDetail(
        val courseLocation: String,
        val weekDayNum: Short,
        val weekNum: Int,
        val timePeriod: TimePeriod
    ) : Serializable
}