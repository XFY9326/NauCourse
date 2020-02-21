package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime

data class CourseCheckResult(
    val isSuccess: Boolean,
    val errorReason: CourseCombineErrorReason = CourseCombineErrorReason.NONE,
    val conflictCourse1: Course? = null,
    val conflictCourseTime1: CourseTime? = null,
    val conflictCourse2: Course? = null,
    val conflictCourseTime2: CourseTime? = null
) {
    enum class CourseCombineErrorReason {
        NONE,
        CONFLICT_TERM,
        CONFLICT_COURSE_TIME
    }
}