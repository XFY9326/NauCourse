package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import java.io.Serializable

data class CourseDetail(
    val course: Course,
    val termDate: TermDate,
    val courseCell: CourseCell,
    val courseCellStyle: CourseCellStyle
) : Serializable