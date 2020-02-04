package tool.xfy9326.naucourses.providers.contents.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper

@Entity(tableName = CoursesDBHelper.COURSES_SCORE_TABLE_NAME)
data class CourseScore(
    @PrimaryKey
    @ColumnInfo(name = CoursesDBHelper.COLUMN_COURSE_ID)
    val courseId: String,
    val name: String,
    val credit: Float,
    val teachClass: String,
    val type: String,
    val property: String,
    val notes: String,
    val ordinaryGrades: Float = DEFAULT_GRADES,
    val midTermGrades: Float = DEFAULT_GRADES,
    val finalTermGrades: Float = DEFAULT_GRADES,
    val overAllGrades: Float = DEFAULT_GRADES,
    val notEntry: Boolean = DEFAULT_ENTRY,
    val notMeasure: Boolean = DEFAULT_MEASURE,
    val notPublish: Boolean = DEFAULT_PUBLISH
) {

    companion object {
        const val DEFAULT_GRADES = 0f
        const val DEFAULT_ENTRY = false
        const val DEFAULT_MEASURE = false
        const val DEFAULT_PUBLISH = false
    }

    override fun hashCode(): Int {
        return courseId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseScore

        if (courseId != other.courseId) return false

        return true
    }
}