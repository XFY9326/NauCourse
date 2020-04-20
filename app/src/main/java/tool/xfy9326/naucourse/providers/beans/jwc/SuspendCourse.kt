package tool.xfy9326.naucourse.providers.beans.jwc

import java.util.*

data class SuspendCourse(
    val name: String,
    val teacher: String,
    val teachClass: String,
    val detail: Array<TimeDetail>
) {
    data class TimeDetail(
        val type: String,
        val time: String,
        val location: String,
        val date: Date
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuspendCourse

        if (name != other.name) return false
        if (teacher != other.teacher) return false
        if (teachClass != other.teachClass) return false
        if (!detail.contentEquals(other.detail)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + teacher.hashCode()
        result = 31 * result + teachClass.hashCode()
        result = 31 * result + detail.contentHashCode()
        return result
    }
}