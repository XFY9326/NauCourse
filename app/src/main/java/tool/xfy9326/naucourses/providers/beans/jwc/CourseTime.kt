package tool.xfy9326.naucourses.providers.beans.jwc

import androidx.room.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper
import java.io.Serializable
import kotlin.math.min

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
    val coursesNumStr: String,
    val coursesNumArray: TimePeriodList,
    val rawCoursesNumStr: String
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

    @Ignore
    private val weeksCharArray = weeksStr.toCharArray()

    @Ignore
    private val coursesNumCharArray = coursesNumStr.toCharArray()

    init {
        if (weekDay !in Constants.Time.MIN_WEEK_DAY..Constants.Time.MAX_WEEK_DAY) {
            throw IllegalArgumentException("Course Time Week Day Error! Week Day: $weekDay")
        }
        if (coursesNumStr.length !in Constants.Course.MIN_COURSE_LENGTH..Constants.Course.MAX_COURSE_LENGTH) {
            throw IllegalArgumentException("Course Time Course Num Length Error! Start Course Num Size: ${coursesNumStr.length}")
        }
        if (weeksStr.length !in Constants.Course.MIN_WEEK_NUM_SIZE..Constants.Course.MAX_WEEK_NUM_SIZE) {
            throw IllegalArgumentException("Course Time Weeks Length Error! Weeks Size: ${weeksStr.length}")
        }
    }

    fun isWeekNumTrue(weekNum: Int): Boolean = TimePeriod.isIndexTrue(weeksCharArray, weekNum - 1)

    fun isCourseNumTrue(courseNum: Int): Boolean = TimePeriod.isIndexTrue(coursesNumCharArray, courseNum - 1)

    fun hasConflict(courseTime: CourseTime): Boolean {
        if (courseTime.weekDay == weekDay &&
            !(courseTime.weekMode == WeekMode.EVEN_WEEK_ONLY && weekMode == WeekMode.ODD_WEEK_ONLY) &&
            !(courseTime.weekMode == WeekMode.ODD_WEEK_ONLY && weekMode == WeekMode.EVEN_WEEK_ONLY)
        ) {
            val weekSize = min(weeksCharArray.size, courseTime.weeksCharArray.size)
            val coursesNumSize = min(coursesNumCharArray.size, courseTime.coursesNumCharArray.size)
            for (weekP in 1..weekSize) {
                if (isWeekNumTrue(weekP) && courseTime.isWeekNumTrue(weekP)) {
                    for (coursesP in 1..coursesNumSize) {
                        if (isCourseNumTrue(coursesP) && courseTime.isCourseNumTrue(coursesP)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseTime

        if (courseId != other.courseId) return false
        if (location != other.location) return false
        if (weeksStr != other.weeksStr) return false
        if (weekMode != other.weekMode) return false
        if (weekDay != other.weekDay) return false
        if (coursesNumStr != other.coursesNumStr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseId.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + weeksStr.hashCode()
        result = 31 * result + weekMode.hashCode()
        result = 31 * result + weekDay
        result = 31 * result + coursesNumStr.hashCode()
        return result
    }
}