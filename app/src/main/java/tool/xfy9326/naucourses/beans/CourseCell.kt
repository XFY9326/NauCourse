package tool.xfy9326.naucourses.beans

data class CourseCell(
    val courseName: String,
    val courseLocation: String,
    val courseId: String,
    val timeDuration: CourseTimeDuration,
    val thisWeekCourse: Boolean
)