package tool.xfy9326.naucourses.providers.beans.jwc

import tool.xfy9326.naucourses.beans.CourseCheckResult
import tool.xfy9326.naucourses.utils.debug.LogUtils
import java.io.Serializable

data class CourseSet(
    private var courses_: HashSet<Course>,
    private var term_: Term
) : Serializable {
    val courses: HashSet<Course> get() = courses_

    // 该值不建议使用，仅作为管理课程时简单的判断课程所在学期
    val term: Term get() = term_

    companion object {
        fun checkCourseTimeConflict(courseSet: HashSet<Course>): CourseCheckResult {
            val timeList = ArrayList<CourseTime>(courseSet.size)
            for (course in courseSet) {
                timeList.addAll(course.timeSet)
            }
            val checkResult = checkCourseTimeConflict(timeList)
            return if (checkResult == null) {
                CourseCheckResult(true)
            } else {
                val course1 = getCourseByCourseTime(courseSet, timeList[checkResult.first])
                val course2 = getCourseByCourseTime(courseSet, timeList[checkResult.second])
                CourseCheckResult(
                    false, CourseCheckResult.CourseCombineErrorReason.CONFLICT_COURSE_TIME,
                    course1, timeList[checkResult.first],
                    course2, timeList[checkResult.second]
                )
            }
        }

        fun checkCourseTimeConflict(courseTimes: Set<CourseTime>): Pair<CourseTime, CourseTime>? {
            val list = courseTimes.toList()
            val result = checkCourseTimeConflict(list)
            if (result != null) {
                return Pair(list[result.first], list[result.second])
            }
            return null
        }

        private fun getCourseByCourseTime(courseSet: HashSet<Course>, courseTime: CourseTime): Course? {
            for (course in courseSet) {
                if (course.timeSet.contains(courseTime)) return course
            }
            return null
        }

        private fun checkCourseTimeConflict(courseTimes: List<CourseTime>): Pair<Int, Int>? {
            for ((i1, courseTime1) in courseTimes.withIndex()) {
                for ((i2, courseTime2) in courseTimes.withIndex()) {
                    if (i1 != i2 && courseTime1.hasConflict(courseTime2)) {
                        return Pair(i1, i2)
                    }
                }
            }
            return null
        }
    }

    // 将会以传入的CourseSet为准
    @Synchronized
    fun update(courseSet: CourseSet): Boolean {
        var hasSame: Boolean
        val newCourses = HashSet(courseSet.courses)
        for (course in courses) {
            hasSame = false
            for (newCourse in courseSet.courses) {
                if (newCourse.id == course.id) {
                    hasSame = true
                    break
                }
            }

            if (!hasSame) {
                newCourses.add(course)
            }
        }
        val checkResult = checkCourseTimeConflict(newCourses)
        return if (checkResult.isSuccess) {
            courses_ = newCourses
            term_ = courseSet.term
            true
        } else {
            LogUtils.d<CourseSet>("Course Update Has Conflicts! Combine Courses Error!\n${checkResult.printText()}")
            false
        }
    }
}