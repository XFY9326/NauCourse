package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.TermDate

data class CoursePkg(
    val termDate: TermDate,
    val courseTable: CourseTable,
    val styles: Array<CourseCellStyle>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoursePkg

        if (termDate != other.termDate) return false
        if (courseTable != other.courseTable) return false
        if (!styles.contentEquals(other.styles)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = termDate.hashCode()
        result = 31 * result + courseTable.hashCode()
        result = 31 * result + styles.contentHashCode()
        return result
    }
}