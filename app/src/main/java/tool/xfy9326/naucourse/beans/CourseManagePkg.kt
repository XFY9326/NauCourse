package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.Term
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate

// 课程管理数据包（用于课程编辑）
data class CourseManagePkg(
    // 学期
    var termDate: TermDate,
    // 课程学期
    var courseTerm: Term,
    // 课程数组
    val courses: ArrayList<Pair<Course, CourseCellStyle>>
) {
    // 从当前数据获取CourseSet
    fun getCourseSet() = CourseSet(HashSet<Course>(courses.size).apply {
        for (pair in courses) {
            add(pair.first)
        }
    }, courseTerm)

    // 从当前数据获取课程样式
    fun getCourseStyleArray() = Array(courses.size) {
        courses[it].second
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseManagePkg

        if (termDate != other.termDate) return false
        if (courseTerm != other.courseTerm) return false
        if (courses != other.courses) return false

        return true
    }

    override fun hashCode(): Int {
        var result = termDate.hashCode()
        result = 31 * result + courseTerm.hashCode()
        result = 31 * result + courses.hashCode()
        return result
    }
}