package tool.xfy9326.naucourses.providers.beans.jwc

data class TimePeriodList(
    val timePeriods: Array<TimePeriod>,
    val size: Int = timePeriods.size
) {

    companion object {
        private const val TIME_PERIOD_ARRAY_JOIN_SYMBOL = ","

        fun parse(str: String): TimePeriodList = if (str.isNotEmpty() && str.isNotBlank()) {
            val text = str.trim()
            if (TIME_PERIOD_ARRAY_JOIN_SYMBOL in text) {
                val splitArray = text.split(TIME_PERIOD_ARRAY_JOIN_SYMBOL)
                val timeArray = arrayOfNulls<TimePeriod>(splitArray.size)
                for ((i, s) in splitArray.withIndex()) {
                    timeArray[i] = TimePeriod.parse(s)
                }
                TimePeriodList(timeArray.requireNoNulls())
            } else {
                TimePeriodList(arrayOf(TimePeriod.parse(text)))
            }
        } else {
            TimePeriodList(emptyArray())
        }
    }

    override fun toString(): String {
        val textBuilder = StringBuilder()
        for (timePeriod in timePeriods) {
            textBuilder.append(timePeriod.toString()).append(TIME_PERIOD_ARRAY_JOIN_SYMBOL)
        }
        textBuilder.deleteCharAt(textBuilder.length - 1)
        return textBuilder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimePeriodList

        if (!timePeriods.contentEquals(other.timePeriods)) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timePeriods.contentHashCode()
        result = 31 * result + size
        return result
    }
}