package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import java.io.Serializable

data class CourseCell(
    val courseId: String,
    val courseName: String,
    val courseTime: CourseTime,
    val weekNum: Int,
    val timeDuration: CourseTimeDuration,
    val thisWeekCourse: Boolean
) : Serializable