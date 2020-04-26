package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import java.io.Serializable

// 课程格
data class CourseCell(
    // 课程ID
    val courseId: String,
    // 课程名称
    val courseName: String,
    // 课程时间
    val courseTime: CourseTime,
    // 周数
    val weekNum: Int,
    // 课程上课时间段
    val timeDuration: CourseTimeDuration,
    // 是否是本周课程
    val thisWeekCourse: Boolean
) : Serializable