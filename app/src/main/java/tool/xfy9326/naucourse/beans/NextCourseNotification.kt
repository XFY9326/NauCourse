package tool.xfy9326.naucourse.beans

data class NextCourseNotification(
    val courseName: String,
    val courseTeacher: String,
    val courseLocation: String,
    val courseStartDateTime: Long,
    val courseEndDateTime: Long
)