package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriod

data class CourseTimeDuration(
    val startTime: Int,
    val durationLength: Int
) {
    companion object {
        fun convertToTimePeriod(duration: CourseTimeDuration): TimePeriod =
            TimePeriod(
                duration.startTime, if (duration.durationLength == 1) {
                    null
                } else {
                    duration.startTime + duration.durationLength - 1
                }
            )

        fun parseTimePeriod(period: TimePeriod): CourseTimeDuration =
            CourseTimeDuration(
                period.start, if (period.hasEnd()) {
                    period.end!! - period.start + 1
                } else {
                    1
                }
            )
    }
}