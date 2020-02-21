package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.Constants

data class ClassTime(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    companion object {
        private const val TIME_JOIN_SYMBOL = ":"
    }

    init {
        if (startHour !in 0..23 || startMinute !in 0..59) {
            throw IllegalArgumentException("Class Time Start Error! ${getStartTimeStr()}")
        }
        if (endHour !in 0..23 || endMinute !in 0..59) {
            throw IllegalArgumentException("Class Time End Error! ${getEndTimeStr()}")
        }
        if (startHour * 60 + startMinute > endHour * 60 + endMinute) {
            throw IllegalArgumentException("Class Time Start and End Error! ${getStartTimeStr()}~${getEndTimeStr()}")
        }
    }

    fun getStartTimeStr() =
        "${String.format(Constants.KEEP_TWO_NUMBER_PLACES, startHour)}$TIME_JOIN_SYMBOL${String.format(
            Constants.KEEP_TWO_NUMBER_PLACES,
            startMinute
        )}"

    fun getEndTimeStr() =
        "${String.format(Constants.KEEP_TWO_NUMBER_PLACES, endHour)}$TIME_JOIN_SYMBOL${String.format(Constants.KEEP_TWO_NUMBER_PLACES, endMinute)}"
}