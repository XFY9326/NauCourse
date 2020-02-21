package tool.xfy9326.naucourses.providers.beans.jwc

import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.beans.CourseCheckResult

data class CourseSet(
    private var courses_: HashSet<Course>,
    val term: Term
) {
    val courses: HashSet<Course> get() = courses_

    init {
        if (courses_.isEmpty()) {
            throw IllegalArgumentException("Course Set Is Empty!")
        }
    }

    companion object {
        fun checkCourseTimeConflict(courseSet: HashSet<Course>): CourseCheckResult {
            val termCourseTable = Array(Constants.Course.MAX_WEEK_NUM_SIZE) {
                Array(Constants.Time.MAX_WEEK_DAY) {
                    arrayOfNulls<Pair<Course, CourseTime>>(
                        Constants.Course.MAX_COURSE_LENGTH
                    )
                }
            }
            for (course in courseSet) {
                for (courseTime in course.timeSet) {
                    for (weekNum in Constants.Course.MIN_WEEK_NUM_SIZE..Constants.Course.MAX_WEEK_NUM_SIZE) {
                        if (courseTime.isWeekNumTrue(weekNum)) {
                            for (courseNum in Constants.Course.MIN_COURSE_LENGTH..Constants.Course.MAX_COURSE_LENGTH) {
                                if (courseTime.isCourseNumTrue(courseNum)) {
                                    if (termCourseTable[weekNum - 1][courseTime.weekDay - 1][courseNum - 1] == null) {
                                        termCourseTable[weekNum - 1][courseTime.weekDay - 1][courseNum - 1] = Pair(course, courseTime)
                                    } else {
                                        val conflictCourse = termCourseTable[weekNum - 1][courseTime.weekDay - 1][courseNum - 1]!!.first
                                        val conflictCourseTime = termCourseTable[weekNum - 1][courseTime.weekDay - 1][courseNum - 1]!!.second
                                        return CourseCheckResult(
                                            false, CourseCheckResult.CourseCombineErrorReason.CONFLICT_COURSE_TIME,
                                            course, courseTime, conflictCourse, conflictCourseTime
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return CourseCheckResult(true)
        }
    }

    @Synchronized
    fun combine(courseSet: CourseSet, ignoreCourseTerm: Boolean = false): CourseCheckResult =
        if (ignoreCourseTerm && this.term != courseSet.term) {
            CourseCheckResult(false, CourseCheckResult.CourseCombineErrorReason.CONFLICT_TERM)
        } else {
            if (courseSet.courses.isEmpty()) {
                CourseCheckResult(true)
            } else {
                val newSet = this.courses_.toHashSet()
                newSet.addAll(courseSet.courses_)
                val result = checkCourseTimeConflict(newSet)
                if (result.isSuccess) {
                    courses_ = newSet
                }
                result
            }
        }
}