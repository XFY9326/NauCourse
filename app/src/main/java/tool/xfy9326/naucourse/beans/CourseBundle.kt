package tool.xfy9326.naucourse.beans

import java.io.Serializable

data class CourseBundle(
    val courseItem: CourseItem,
    val courseCellStyle: CourseCellStyle
) : Serializable