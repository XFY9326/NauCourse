package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.constants.CourseConst
import tool.xfy9326.naucourse.constants.DBConst
import tool.xfy9326.naucourse.io.db.TermDateDBHelper
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import java.io.Serializable
import java.util.*

@Entity(tableName = TermDateDBHelper.TERM_DATE_TABLE_NAME)
data class TermDate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConst.COLUMN_ID)
    val id: Int,
    // 周数为 0 表示假期
    var currentWeekNum: Int,
    val startDate: Date,
    val endDate: Date,
    var inVacation: Boolean
) : Serializable {
    constructor(currentWeekNum: Int, startDate: Date, endDate: Date, inVacation: Boolean = (currentWeekNum <= 0)) :
            this(DBConst.DEFAULT_ID, currentWeekNum, startDate, endDate, inVacation)

    constructor(startDate: Date, endDate: Date) :
            this(TimeUtils.getWeekNum(startDate, endDate), startDate, endDate)

    init {
        if (currentWeekNum < 0 || currentWeekNum > CourseConst.MAX_WEEK_NUM_SIZE) {
            throw IllegalArgumentException("Term Info Week Number Error! Week Num: $currentWeekNum")
        }
        if (endDate.time <= startDate.time) {
            throw IllegalArgumentException("Term Info Term Start And End Date Error! Start Date: $startDate  End Date: $endDate")
        }
    }

    companion object {
        fun getTerm(startDate: Date): Term {
            val calendar = Calendar.getInstance(Locale.CHINA)
            calendar.time = startDate

            val year = calendar[Calendar.YEAR]

            return if (calendar[Calendar.MONTH] + 1 > 6) {
                Term(year, year + 1, 1)
            } else {
                Term(year - 1, year, 2)
            }
        }
    }

    fun inVacation(date: Date) = date.time < startDate.time || endDate.time + 24 * 60 * 60 * 1000 <= date.time

    fun refreshCurrentWeekNum() {
        currentWeekNum = TimeUtils.getWeekNum(startDate, endDate)
        inVacation = currentWeekNum <= 0
    }

    fun getTerm() = Companion.getTerm(startDate)
}