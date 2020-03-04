package tool.xfy9326.naucourses.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper

@Entity(tableName = CoursesDBHelper.TERM_TABLE_NAME)
data class Term(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
    val id: Int,
    val startYear: Int,
    val endYear: Int,
    val termNum: Short
) {
    constructor(startYear: Int, endYear: Int, termNum: Short) : this(Constants.DB.DEFAULT_ID, startYear, endYear, termNum)

    init {
        if (endYear < 0 || startYear < 0 || endYear - startYear != 1) {
            throw IllegalArgumentException("Term Year Error! Start: $startYear End: $endYear")
        }
        if (termNum !in 1..2) {
            throw IllegalArgumentException("Term Num Error! TermNum: $termNum")
        }
    }

    companion object {
        fun parse(text: String): Term {
            val mid = (text.length - 1) / 2
            val startYear = text.substring(0, mid).toInt()
            val endYear = text.substring(mid, text.length - 1).toInt()
            val termNum = text.substring(text.length - 1, text.length).toShort()
            return Term(startYear, endYear, termNum)
        }
    }

    operator fun compareTo(term: Term): Int {
        val startCompare = startYear.compareTo(term.startYear)
        return if (startCompare == 0) {
            termNum.compareTo(term.termNum)
        } else {
            startCompare
        }
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other is Term) {
            other.toString() == toString()
        } else {
            false
        }
    }

    override fun toString(): String {
        return "$startYear$endYear$termNum"
    }
}