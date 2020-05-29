package tool.xfy9326.naucourse.providers.beans.jwc

import java.io.Serializable
import java.util.*
import kotlin.math.max

data class TimePeriod(
    val start: Int,
    private var end_: Int? = null
) : Serializable {
    val end: Int? get() = end_

    init {
        if (start == end_) {
            end_ = null
        }
        if (end_ != null && start > end_!!) {
            throw IllegalArgumentException("Time Period Error! Start is Larger than End! Start: $start End: $end_")
        }
    }

    fun hasEnd() = end_ != null

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

        fun initCharArray(size: Int, source: CharArray? = null): CharArray =
            CharArray(size) { if (source != null && it < source.size - 1) source[it] else False }

        fun isIndexTrue(arr: CharArray, i: Int) = i >= 0 && i < arr.size && arr[i] == True
    }

    fun convertToCharArray(
        initCharLength: Int, oddMode: Boolean = false, evenMode: Boolean = false, countFromZero: Boolean = false, source: CharArray? = null
    ): CharArray {
        val arrLength = max(
            initCharLength, (end_ ?: start) + if (countFromZero) {
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
        val prefixEnd = (end_ ?: start) - prefix
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimePeriod

        if (start != other.start) return false
        if (end_ != other.end_) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + (end_ ?: 0)
        return result
    }
}