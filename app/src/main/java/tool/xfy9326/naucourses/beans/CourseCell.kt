package tool.xfy9326.naucourses.beans

data class CourseCell(
    val courseId: String,
    val courseName: String,
    val courseLocation: String,
    val weekDayNum: Short,
    val timeDuration: CourseTimeDuration,
    val thisWeekCourse: Boolean
)