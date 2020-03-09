package tool.xfy9326.naucourses.providers.beans.jwc

import tool.xfy9326.naucourses.beans.CourseCheckResult
import tool.xfy9326.naucourses.utils.utility.LogUtils

data class CourseSet(
    private var courses_: HashSet<Course>,
    private var term_: Term
) {
    val courses: HashSet<Course> get() = courses_
    val term: Term get() = term_

    init {
        if (courses_.isEmpty()) {
            throw IllegalArgumentException("Course Set Is Empty!")
        }
    }

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

    @Synchronized
    fun update(courseSet: CourseSet): Boolean =
        if (term < courseSet.term) {
            val checkResult = checkCourseTimeConflict(courseSet.courses)
            if (checkResult.isSuccess) {
                term_ = courseSet.term
                courses_ = courseSet.courses
                true
            } else {
                LogUtils.d<CourseSet>("Course Update Has Conflicts! Replace New Term Courses Error!\n${checkResult.printText()}")
                false
            }
        } else if (term == courseSet.term) {
            val newCourses = HashSet(courseSet.courses)
            newCourses.addAll(courses)
            val checkResult = checkCourseTimeConflict(newCourses)
            if (checkResult.isSuccess) {
                courses_ = newCourses
                true
            } else {
                LogUtils.d<CourseSet>("Course Update Has Conflicts! Combine Courses Error!\n${checkResult.printText()}")
                false
            }
        } else {
            true
        }
}