package tool.xfy9326.naucourse.providers.beans.jwc

import java.util.*

data class EmptyRoomInfo(
    val campusName: Array<Pair<String, String>>,
    val term: String,
    val startDate: Date,
    val endDate: Date,
    val BJC: Array<Time>,
    val EJC: Array<Time>
) {
    data class Time(
        val num: Int,
        val text: String
    ) {
        override fun toString() = text
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmptyRoomInfo

        if (!campusName.contentEquals(other.campusName)) return false
        if (term != other.term) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (!BJC.contentEquals(other.BJC)) return false
        if (!EJC.contentEquals(other.EJC)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = campusName.contentHashCode()
        result = 31 * result + term.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + BJC.contentHashCode()
        result = 31 * result + EJC.contentHashCode()
        return result
    }
}