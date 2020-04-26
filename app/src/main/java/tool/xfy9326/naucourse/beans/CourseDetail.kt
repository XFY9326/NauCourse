package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriod
import java.io.Serializable

// 课程详情（用于课程详情Dialog使用）
data class CourseDetail(
    // 课程
    val course: Course,
    // 学期
    val termDate: TermDate,
    // 课程格风格
    val courseCellStyle: CourseCellStyle,
    // 时间详情（若不存在则不显示下节课时间）
    val timeDetail: TimeDetail? = null
) : Serializable {

    // 时间详情
    data class TimeDetail(
        // 课程地点
        val courseLocation: String,
        // 星期
        val weekDayNum: Short,
        // 周数
        val weekNum: Int,
        // 时间周期
        val timePeriod: TimePeriod
    ) : Serializable
}