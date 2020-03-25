package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.dbHelpers.JwcDBHelper
import tool.xfy9326.naucourse.utils.compute.TimeUtils
import java.io.Serializable
import java.util.*

@Entity(tableName = JwcDBHelper.TERM_DATE_TABLE_NAME)
data class TermDate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
    val id: Int,
    // 周数为 0 表示假期
    var currentWeekNum: Int,
    val startDate: Date,
    val endDate: Date,
    var inVacation: Boolean
) : Serializable {
    constructor(currentWeekNum: Int, startDate: Date, endDate: Date, inVacation: Boolean = (currentWeekNum <= 0)) :
            this(Constants.DB.DEFAULT_ID, currentWeekNum, startDate, endDate, inVacation)

    constructor(startDate: Date, endDate: Date) :
            this(TimeUtils.getWeekNum(startDate, endDate), startDate, endDate)

    init {
        if (currentWeekNum < 0 || currentWeekNum > Constants.Course.MAX_WEEK_NUM_SIZE) {
            throw IllegalArgumentException("Term Info Week Number Error! Week Num: $currentWeekNum")
        }
        if (endDate.time <= startDate.time) {
            throw IllegalArgumentException("Term Info Term Start And End Date Error! Start Date: $startDate  End Date: $endDate")
        }
    }

    companion object {
        fun generateNewTermDate(): TermDate {
            Calendar.getInstance(Locale.CHINA).apply {
                time = Date()

                set(Calendar.MINUTE, 0)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                return if (get(Calendar.MONTH) + 1 > 6) {
                    set(Calendar.MONTH, Calendar.SEPTEMBER)
                    set(Calendar.DATE, 1)
                    val start = time
                    add(Calendar.YEAR, 1)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DATE, 15)

                    TermDate(start, time)
                } else {
                    set(Calendar.MONTH, Calendar.MARCH)
                    set(Calendar.DATE, 1)
                    val start = time
                    set(Calendar.MONTH, Calendar.JUNE)
                    set(Calendar.DATE, 15)

                    TermDate(start, time)
                }
            }
        }
    }

    fun refreshCurrentWeekNum() {
        currentWeekNum = TimeUtils.getWeekNum(startDate, endDate)
        inVacation = currentWeekNum <= 0
    }

    fun getTerm(): Term {
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