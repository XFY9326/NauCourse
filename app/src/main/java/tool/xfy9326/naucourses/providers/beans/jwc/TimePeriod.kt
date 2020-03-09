package tool.xfy9326.naucourses.providers.beans.jwc

import java.io.Serializable
import java.util.*
import kotlin.math.max

data class TimePeriod(
    val start: Int,
    val end: Int? = null
) : Serializable {
    init {
        if (end != null && start > end) {
            throw IllegalArgumentException("Time Period Error! Start is Larger than End! Start: $start End: $end")
        }
    }

    fun hasEnd() = end != null

    companion object {
        private const val TIME_PERIOD_JOIN_SYMBOL = "-"

        private const val True = '1'
        private const val False = '0'

        fun parse(str: String): TimePeriod = if (str.isNotEmpty() && str.isNotBlank()) {
            val text = str.trim()
            if (TIME_PERIOD_JOIN_SYMBOL in text) {
                val splitArr = text.split(TIME_PERIOD_JOIN_SYMBOL)
                TimePeriod(splitArr[0].toInt(), splitArr[1].toInt())
            } else {
                TimePeriod(text.toInt())
            }
        } else {
            throw IllegalArgumentException("Empty Parse Text to Time Period!")
        }

        private fun initCharArray(size: Int, source: CharArray? = null): CharArray =
            CharArray(size) { if (source != null && it < source.size - 1) source[it] else False }

        fun isIndexTrue(arr: CharArray, i: Int) = i >= 0 && i < arr.size && arr[i] == True
    }

    fun convertToCharArray(
        initCharLength: Int, oddMode: Boolean = false, evenMode: Boolean = false, countFromZero: Boolean = false, source: CharArray? = null
    ):
            CharArray {
        val arrLength = max(
            initCharLength, (end ?: start) + if (countFromZero) {
                1
            } else {
                0
            }
        )
        val prefix = if (countFromZero) {
            0
        } else {
            1
        }
        val array = initCharArray(arrLength, source)
        val prefixStart = start - prefix
        val prefixEnd = (end ?: start) - prefix
        if (oddMode && !evenMode) {
            for (i in prefixStart..prefixEnd) if ((i + 1) % 2 != 0) array[i] = True
        } else if (evenMode && !oddMode) {
            for (i in prefixStart..prefixEnd) if ((i + 1) % 2 == 0) array[i] = True
        } else {
            Arrays.fill(array, prefixStart, prefixEnd + 1, True)
        }
        return array
    }

    override fun toString(): String =
        if (hasEnd()) {
            "$start$TIME_PERIOD_JOIN_SYMBOL$end"
        } else {
            start.toString()
        }
}