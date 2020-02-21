package tool.xfy9326.naucourses.beans

data class CourseTable(
    val table: Array<Array<CourseCell>>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseTable

        if (!table.contentEquals(other.table)) return false

        return true
    }

    override fun hashCode(): Int {
        return table.contentHashCode()
    }
}