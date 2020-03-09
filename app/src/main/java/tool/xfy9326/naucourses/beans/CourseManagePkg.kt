package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.Term
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate

@Suppress("ArrayInDataClass")
data class CourseManagePkg(
    var termDate: TermDate,
    var courseTerm: Term,
    val courses: ArrayList<Pair<Course, CourseCellStyle>>
) {
    fun getCourseSet() = CourseSet(HashSet<Course>(courses.size).apply {
        for (pair in courses) {
            add(pair.first)
        }
    }, courseTerm)

    fun getCourseStyleArray() = Array(courses.size) {
        courses[it].second
    }
}