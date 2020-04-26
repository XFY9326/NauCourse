package tool.xfy9326.naucourse.beans

import java.io.Serializable

// 课程数据包
data class CourseBundle(
    // 课程数据
    val courseItem: CourseItem,
    // 课程格风格
    val courseCellStyle: CourseCellStyle
) : Serializable