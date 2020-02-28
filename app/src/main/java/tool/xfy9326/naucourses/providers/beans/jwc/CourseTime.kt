package tool.xfy9326.naucourses.providers.beans.jwc

import androidx.room.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper
import java.io.Serializable

@Entity(
    tableName = CoursesDBHelper.COURSES_TIME_TABLE_NAME,
    indices = [Index(value = [CoursesDBHelper.COLUMN_COURSE_ID]), Index(value = [CoursesDBHelper.COLUMN_WEEK_DAY])],
    foreignKeys = [ForeignKey(entity = Course::class, parentColumns = [Constants.DB.COLUMN_ID], childColumns = [CoursesDBHelper.COLUMN_COURSE_ID])]
)
data class CourseTime(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
    val id: Int,
    var courseId: String,
    val location: String,
    val weeksStr: String,
    val weekMode: WeekMode,
    val weeksArray: TimePeriodList,
    val rawWeeksStr: String,
    @ColumnInfo(name = CoursesDBHelper.COLUMN_WEEK_DAY)
    val weekDay: Short,
    val courseNumStr: String,
    val coursesNumArray: TimePeriodList,
    val rawCourseNumStr: String
) : Serializable {
    constructor(
        courseId: String,
        location: String,
        weeksStr: String,
        weekMode: WeekMode,
        weeksArray: TimePeriodList,
        rawWeeksStr: String,
        weekDay: Short,
        courseNumStr: String,
        coursesNumArray: TimePeriodList,
        rawCourseNumStr: String
    ) : this(
        Constants.DB.DEFAULT_ID, courseId, location, weeksStr, weekMode, weeksArray, rawWeeksStr, weekDay, courseNumStr,
        coursesNumArray,
        rawCourseNumStr
    )

    init {
        if (weekDay !in Constants.Time.MIN_WEEK_DAY..Constants.Time.MAX_WEEK_DAY) {
            throw IllegalArgumentException("Course Time Week Day Error! Week Day: $weekDay")
        }
        if (courseNumStr.length !in Constants.Course.MIN_COURSE_LENGTH..Constants.Course.MAX_COURSE_LENGTH) {
            throw IllegalArgumentException("Course Time Course Num Length Error! Start Course Num Size: ${courseNumStr.length}")
        }
        if (weeksStr.length !in Constants.Course.MIN_WEEK_NUM_SIZE..Constants.Course.MAX_WEEK_NUM_SIZE) {
            throw IllegalArgumentException("Course Time Weeks Length Error! Weeks Size: ${weeksStr.length}")
        }
    }

    fun isWeekNumTrue(weekNum: Int): Boolean =
        TimePeriod.isStrIndexTrue(weeksStr, weekNum - 1)

    fun isCourseNumTrue(courseNum: Int): Boolean =
        TimePeriod.isStrIndexTrue(courseNumStr, courseNum - 1)
}