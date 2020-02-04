package tool.xfy9326.naucourses.providers.contents.beans.jwc

import androidx.room.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper
import java.util.*

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
    val rawWeeksStr: String,
    @ColumnInfo(name = CoursesDBHelper.COLUMN_WEEK_DAY)
    val weekDay: Short,
    val courseNumStr: String,
    val rawCourseNumStr: String
) {
    constructor(
        courseId: String,
        location: String,
        weeksStr: String,
        rawWeeksStr: String,
        weekDay: Short,
        courseNumStr: String,
        rawCourseNumStr: String
    ) : this(Constants.DB.DEFAULT_ID, courseId, location, weeksStr, rawWeeksStr, weekDay, courseNumStr, rawCourseNumStr)

    init {
        if (weekDay !in Constants.Time.MIN_WEEK_DAY..Constants.Time.MAX_WEEK_DAY) {
            throw IllegalArgumentException("Course Time Week Day Error! Week Day: $weekDay")
        }
        if (courseNumStr.length !in Constants.Course.MIN_COURSE_LENGTH..Constants.Course.MAX_COURSE_LENGTH) {
            throw IllegalArgumentException("Course Time Course Num Length Error! Start Course Num Size: ${courseNumStr.length}")
        }
        if (weeksStr.length !in Constants.Course.MIN_WEEKS_SIZE..Constants.Course.MAX_WEEKS_SIZE) {
            throw IllegalArgumentException("Course Time Weeks Length Error! Weeks Size: ${weeksStr.length}")
        }
    }

    companion object {
        const val True = '1'
        const val False = '0'

        fun getDefaultWeeksArray() =
            initCharArray(Constants.Course.MAX_WEEKS_SIZE)

        fun getDefaultCourseNumArray() =
            initCharArray(Constants.Course.MAX_COURSE_LENGTH)

        private fun initCharArray(size: Int): CharArray {
            val result = CharArray(size)
            Arrays.fill(result, False)
            return result
        }

        private fun isStrIndexTrue(str: String, i: Int) = i < str.length && str[i] == True
    }

    fun isWeekNumTrue(weekNum: Int): Boolean =
        isStrIndexTrue(weeksStr, weekNum - 1)

    fun isCourseNumTrue(courseNum: Int): Boolean =
        isStrIndexTrue(courseNumStr, courseNum - 1)
}