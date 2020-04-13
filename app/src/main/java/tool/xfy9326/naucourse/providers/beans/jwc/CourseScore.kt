package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.io.db.CourseScoreDBHelper

@Entity(tableName = CourseScoreDBHelper.COURSES_SCORE_TABLE_NAME)
data class CourseScore(
    @PrimaryKey
    @ColumnInfo(name = CourseScoreDBHelper.COLUMN_COURSE_ID)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseScore

        if (courseId != other.courseId) return false
        if (name != other.name) return false
        if (credit != other.credit) return false
        if (teachClass != other.teachClass) return false
        if (type != other.type) return false
        if (property != other.property) return false
        if (notes != other.notes) return false
        if (ordinaryGrades != other.ordinaryGrades) return false
        if (midTermGrades != other.midTermGrades) return false
        if (finalTermGrades != other.finalTermGrades) return false
        if (overAllGrades != other.overAllGrades) return false
        if (notEntry != other.notEntry) return false
        if (notMeasure != other.notMeasure) return false
        if (notPublish != other.notPublish) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + credit.hashCode()
        result = 31 * result + teachClass.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + property.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + ordinaryGrades.hashCode()
        result = 31 * result + midTermGrades.hashCode()
        result = 31 * result + finalTermGrades.hashCode()
        result = 31 * result + overAllGrades.hashCode()
        result = 31 * result + notEntry.hashCode()
        result = 31 * result + notMeasure.hashCode()
        result = 31 * result + notPublish.hashCode()
        return result
    }
}