package tool.xfy9326.naucourses.beans

import java.util.*

data class DateTimePeriod(
    val startDateTime: Date,
    val endDateTime: Date
) {
    init {
        if (startDateTime > endDateTime) {
            throw IllegalArgumentException("Date Time Period Start Date Time is Larger than End Date Time!")
        }
    }
}