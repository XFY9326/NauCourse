package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime

data class CourseArrange(
    val todayCourseArr: Array<Pair<CourseItem, CourseCellStyle>>,
    val tomorrowCourseArr: Array<Pair<CourseItem, CourseCellStyle>>,
    val notThisWeekCourseArr: Array<Triple<Course, CourseTime, CourseCellStyle>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseArrange

        if (!todayCourseArr.contentEquals(other.todayCourseArr)) return false
        if (!tomorrowCourseArr.contentEquals(other.tomorrowCourseArr)) return false
        if (!notThisWeekCourseArr.contentEquals(other.notThisWeekCourseArr)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = todayCourseArr.contentHashCode()
        result = 31 * result + tomorrowCourseArr.contentHashCode()
        result = 31 * result + notThisWeekCourseArr.contentHashCode()
        return result
    }
}