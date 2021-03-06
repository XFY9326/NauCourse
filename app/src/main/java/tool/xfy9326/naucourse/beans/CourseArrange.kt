package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.TermDate

// 课程安排，用于数据传递与保存
data class CourseArrange(
    // 今日课程
    val todayCourseArr: Array<CourseBundle>,
    // 明日课程
    val tomorrowCourseArr: Array<CourseBundle>,
    // 非本周课程
    val notThisWeekCourseArr: Array<CourseBundle>,
    // 学期
    val termDate: TermDate?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseArrange

        if (!todayCourseArr.contentEquals(other.todayCourseArr)) return false
        if (!tomorrowCourseArr.contentEquals(other.tomorrowCourseArr)) return false
        if (!notThisWeekCourseArr.contentEquals(other.notThisWeekCourseArr)) return false
        if (termDate != other.termDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = todayCourseArr.contentHashCode()
        result = 31 * result + tomorrowCourseArr.contentHashCode()
        result = 31 * result + notThisWeekCourseArr.contentHashCode()
        result = 31 * result + (termDate?.hashCode() ?: 0)
        return result
    }
}