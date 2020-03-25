package tool.xfy9326.naucourse.beans

import java.io.Serializable

data class CourseCell(
    val courseId: String,
    val courseName: String,
    val courseLocation: String,
    val weekDayNum: Short,
    val weekNum: Int,
    val timeDuration: CourseTimeDuration,
    val thisWeekCourse: Boolean
) : Serializable