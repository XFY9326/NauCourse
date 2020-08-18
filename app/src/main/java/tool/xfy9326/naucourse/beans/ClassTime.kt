package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.constants.BaseConst

// 上课时间
data class ClassTime(
    // 开始小时
    val startHour: Int,
    // 开始分钟
    val startMinute: Int,
    // 结束小时
    val endHour: Int,
    // 结束分钟
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

    // 获取开始时间的字符串 HH:mm
    fun getStartTimeStr() =
        "${String.format(BaseConst.KEEP_TWO_NUMBER_PLACES, startHour)}$TIME_JOIN_SYMBOL${
            String.format(
                BaseConst.KEEP_TWO_NUMBER_PLACES,
                startMinute
            )
        }"

    // 获取结束时间的字符串 HH:mm
    fun getEndTimeStr() =
        "${String.format(BaseConst.KEEP_TWO_NUMBER_PLACES, endHour)}$TIME_JOIN_SYMBOL${
            String.format(
                BaseConst.KEEP_TWO_NUMBER_PLACES, endMinute
            )
        }"
}