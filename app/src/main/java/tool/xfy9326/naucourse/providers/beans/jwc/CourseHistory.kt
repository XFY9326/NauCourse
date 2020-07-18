package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.constants.DBConst
import tool.xfy9326.naucourse.io.db.CourseHistoryDBHelper

@Entity(tableName = CourseHistoryDBHelper.COURSES_HISTORY_TABLE_NAME)
data class CourseHistory(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConst.COLUMN_ID)
    val id: Int,
    val courseId: String,
    val name: String,
    val credit: Float,
    val score: Float?,
    val scoreRawText: String,
    val creditWeight: Float,
    val term: Term,
    val courseProperty: String,
    val academicProperty: String,
    val type: String,
    val notes: String
) {
    constructor(
        courseId: String, name: String, credit: Float, score: Float?, scoreRawText: String,
        creditWeight: Float, term: Term, courseProperty: String, academicProperty: String, type: String, notes: String
    ) :
            this(
                DBConst.DEFAULT_ID, courseId, name, credit, score, scoreRawText, creditWeight,
                term, courseProperty, academicProperty, type, notes
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseHistory

        if (courseId != other.courseId) return false
        if (name != other.name) return false
        if (credit != other.credit) return false
        if (score != other.score) return false
        if (scoreRawText != other.scoreRawText) return false
        if (creditWeight != other.creditWeight) return false
        if (term != other.term) return false
        if (courseProperty != other.courseProperty) return false
        if (academicProperty != other.academicProperty) return false
        if (type != other.type) return false
        if (notes != other.notes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + credit.hashCode()
        result = 31 * result + (score?.hashCode() ?: 0)
        result = 31 * result + scoreRawText.hashCode()
        result = 31 * result + creditWeight.hashCode()
        result = 31 * result + term.hashCode()
        result = 31 * result + courseProperty.hashCode()
        result = 31 * result + academicProperty.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }
}