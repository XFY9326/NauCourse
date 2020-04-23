package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.ui.views.table.CourseTableStyle

data class CoursePkg(
    val termDate: TermDate,
    val courseTable: CourseTable,
    val styles: Array<CourseCellStyle>,
    val courseTableStyle: CourseTableStyle
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoursePkg

        if (termDate != other.termDate) return false
        if (courseTable != other.courseTable) return false
        if (!styles.contentEquals(other.styles)) return false
        if (courseTableStyle != other.courseTableStyle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = termDate.hashCode()
        result = 31 * result + courseTable.hashCode()
        result = 31 * result + styles.contentHashCode()
        result = 31 * result + courseTableStyle.hashCode()
        return result
    }
}