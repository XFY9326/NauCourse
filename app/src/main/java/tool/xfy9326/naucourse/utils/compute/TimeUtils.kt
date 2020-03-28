package tool.xfy9326.naucourse.utils.compute

import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.ClassTime
import tool.xfy9326.naucourse.beans.DateTimePeriod
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriod
import java.util.*
import kotlin.math.ceil

object TimeUtils {
    val CLASS_TIME_ARR = arrayOf(
        ClassTime(8, 30, 9, 10),
        ClassTime(9, 20, 10, 0),
        ClassTime(10, 20, 11, 0),
        ClassTime(11, 10, 11, 50),
        ClassTime(12, 0, 12, 40),
        ClassTime(13, 30, 14, 10),
        ClassTime(14, 20, 15, 0),
        ClassTime(15, 20, 16, 0),
        ClassTime(16, 10, 16, 50),
        ClassTime(17, 0, 17, 40),
        ClassTime(18, 30, 19, 10),
        ClassTime(19, 20, 20, 0),
        ClassTime(20, 10, 20, 50)
    )

    private fun getNewCalendar(date: Date? = null): Calendar {
        val calendar = Calendar.getInstance(Locale.CHINA)
        calendar.firstDayOfWeek = Calendar.MONDAY
        if (date != null) calendar.time = date
        return calendar
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getCourseDate(termStartDate: Date, weekNum: Int, weekDay: Short): Date = getNewCalendar(
        termStartDate
    ).apply {
        val dayOffset = 1 - getWeekDayNum(this) + (weekNum - 1) * 7 + weekDay - 1
        if (dayOffset != 0) add(Calendar.DATE, dayOffset)
        set(Calendar.MINUTE, 0)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    fun getCourseDateTimePeriod(courseDate: Date, timePeriod: TimePeriod): DateTimePeriod {
        if (timePeriod.start < Constants.Course.MIN_COURSE_LENGTH || timePeriod.hasEnd() && timePeriod.end!! > Constants.Course.MAX_COURSE_LENGTH) {
            throw IllegalArgumentException("Invalid Course Time Period! $timePeriod")
        }
        val startDateTime: Date
        val endDateTime: Date
        getNewCalendar(courseDate).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            set(Calendar.MINUTE, CLASS_TIME_ARR[timePeriod.start - 1].startMinute)
            set(Calendar.HOUR_OF_DAY, CLASS_TIME_ARR[timePeriod.start - 1].startHour)
            startDateTime = time
            if (timePeriod.hasEnd()) {
                set(Calendar.MINUTE, CLASS_TIME_ARR[timePeriod.end!! - 1].endMinute)
                set(Calendar.HOUR_OF_DAY, CLASS_TIME_ARR[timePeriod.end!! - 1].endHour)
            } else {
                set(Calendar.MINUTE, CLASS_TIME_ARR[timePeriod.start - 1].endMinute)
                set(Calendar.HOUR_OF_DAY, CLASS_TIME_ARR[timePeriod.start - 1].endHour)
            }
            endDateTime = time
        }
        return DateTimePeriod(startDateTime, endDateTime)
    }

    fun getTodayDate(): Pair<Int, Int> {
        getNewCalendar().apply {
            return Pair(get(Calendar.MONTH) + 1, get(Calendar.DATE))
        }
    }

    fun getCourseDateTimePeriod(termStartDate: Date, weekNum: Int, weekDay: Short, timePeriod: TimePeriod): DateTimePeriod =
        getCourseDateTimePeriod(
            getCourseDate(
                termStartDate,
                weekNum,
                weekDay
            ), timePeriod
        )

    fun getWeekNumDateArray(termDate: TermDate, weekNum: Int): Pair<Int, Array<Int>> {
        val calendar = getNewCalendar(termDate.startDate).apply {
            // 最后 -1 是为了生成日期时方便
            val dayOffset = 1 - getWeekDayNum(this) + (weekNum - 1) * 7 - 1
            if (dayOffset != 0) add(Calendar.DATE, dayOffset)
        }
        return Pair(calendar.get(Calendar.MONTH) + 1, Array(Constants.Time.MAX_WEEK_DAY) {
            calendar.add(Calendar.DATE, 1)
            calendar.get(Calendar.DATE)
        })
    }

    fun inWeekend() = Calendar.getInstance(Locale.CHINA).get(Calendar.DAY_OF_WEEK).let {
        return@let it == Calendar.SATURDAY || it == Calendar.SUNDAY
    }

    fun getWeekDayNum(date: Date) = getWeekDayNum(getNewCalendar(date))

    fun getWeekDayNum(calendar: Calendar): Int {
        calendar.apply {
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            return if (dayOfWeek == Calendar.SUNDAY) {
                7
            } else {
                dayOfWeek - 1
            }
        }
    }

    fun getWeekNum(termDate: TermDate, calculateDate: Date = Date()): Int =
        getWeekNum(termDate.startDate, termDate.endDate, calculateDate)

    fun getWeekNum(startDate: Date, endDate: Date, calculateDate: Date = Date()): Int {
        val startCalendar = getFixedTermStartDateCalendar(startDate)
        val endCalendar = getFixedTermEndDateCalendar(endDate)
        return if (calculateDate.time >= startCalendar.timeInMillis && calculateDate.time <= endCalendar.timeInMillis) {
            ceil((calculateDate.time - startCalendar.timeInMillis) / (7 * 24 * 60 * 60 * 1000f)).toInt()
        } else {
            0
        }
    }

    fun getWeekLength(termDate: TermDate, getRawLength: Boolean = false): Int {
        val startMills = if (getRawLength) termDate.startDate.time else getFixedTermStartDateCalendar(termDate.startDate).timeInMillis
        val endMills = if (getRawLength) termDate.endDate.time else getFixedTermEndDateCalendar(termDate.endDate).timeInMillis
        return if (getRawLength) {
            ((endMills - startMills + 24 * 60 * 60 * 1000f) / (7 * 24 * 60 * 60 * 1000f)).toInt()
        } else {
            ceil((endMills - startMills) / (7 * 24 * 60 * 60 * 1000f)).toInt()
        }
    }

    private fun getFixedTermEndDateCalendar(date: Date) =
        getNewCalendar(date).apply {
            val dayOffset = 7 - getWeekDayNum(this)
            if (dayOffset != 0) add(Calendar.DATE, dayOffset)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private fun getFixedTermStartDateCalendar(date: Date) =
        getNewCalendar(date).apply {
            val dayOffset = 1 - getWeekDayNum(this)
            if (dayOffset != 0) add(Calendar.DATE, dayOffset)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
}