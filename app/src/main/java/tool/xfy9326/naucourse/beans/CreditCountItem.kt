package tool.xfy9326.naucourse.beans

import java.io.Serializable

data class CreditCountItem(
    val score: Float,
    val credit: Float,
    val courseId: String?,
    val courseName: String?,
    val creditWeight: Float = 1f,
    var isSelected: Boolean = true
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreditCountItem

        if (courseId != other.courseId) return false
        if (courseName != other.courseName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseId?.hashCode() ?: 0
        result = 31 * result + (courseName?.hashCode() ?: 0)
        return result
    }
}