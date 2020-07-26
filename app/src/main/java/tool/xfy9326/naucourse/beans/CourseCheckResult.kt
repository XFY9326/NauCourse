package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime

// 课程冲突检查结果
data class CourseCheckResult(
    // 是否有冲突（无冲突则下面的数据为空）
    val isSuccess: Boolean,
    // 错误原因
    val errorReason: CourseCombineErrorReason = CourseCombineErrorReason.NONE,
    // 冲突课程1
    val conflictCourse1: Course? = null,
    // 冲突课程时间1
    val conflictCourseTime1: CourseTime? = null,
    // 冲突课程2
    val conflictCourse2: Course? = null,
    // 冲突课程时间2
    val conflictCourseTime2: CourseTime? = null
) {
    enum class CourseCombineErrorReason {
        NONE,
        CONFLICT_COURSE_TIME
    }

    // 输出冲突检测结果，仅用于控制台调试使用
    fun printText() =
        if (isSuccess) {
            "Courses Have No Conflicts"
        } else {
            "Courses Have Conflicts! Error Reason: $errorReason\n" +
                    "Course 1: ${conflictCourse1!!.name}  ${conflictCourseTime1!!.rawWeeksStr} ${conflictCourseTime1.rawCoursesNumStr}\n" +
                    "Course 2: ${conflictCourse2!!.name}  ${conflictCourseTime2!!.rawWeeksStr} ${conflictCourseTime2.rawCoursesNumStr}"
        }
}