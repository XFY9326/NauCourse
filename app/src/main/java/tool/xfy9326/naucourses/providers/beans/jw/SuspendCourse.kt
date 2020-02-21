package tool.xfy9326.naucourses.providers.beans.jw

import java.util.*

// TODO("SuspendCourse")
data class SuspendCourse(
    val name: String,
    val teacher: String,
    val teachClass: String,
    val type: String,
    val num: Int,
    val location: String,
    val date: Date
)