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
        CONFLICT_COURSE_TIME
    }

    fun printText() =
        if (isSuccess) {
            "Courses Has No Conflicts"
        } else {
            "Courses Has Conflicts  Error Reason: $errorReason\n" +
                    "Course 1: ${conflictCourse1!!.name}  ${conflictCourseTime1!!.rawWeeksStr} ${conflictCourseTime1.rawCoursesNumStr}\n" +
                    "Course 2: ${conflictCourse2!!.name}  ${conflictCourseTime2!!.rawWeeksStr} ${conflictCourseTime2.rawCoursesNumStr}"
        }
}