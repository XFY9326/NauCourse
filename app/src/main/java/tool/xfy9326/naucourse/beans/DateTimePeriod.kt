package tool.xfy9326.naucourse.beans

import java.io.Serializable
import java.util.*

// 时间间隔
data class DateTimePeriod(
    // 开始时间
    val startDateTime: Date,
    // 结束时间
    val endDateTime: Date
) : Serializable {
    init {
        if (startDateTime > endDateTime) {
            throw IllegalArgumentException("Date Time Period Start Date Time is Larger than End Date Time!")
        }
    }
}