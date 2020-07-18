package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.constants.DBConst
import tool.xfy9326.naucourse.io.db.ExamDBHelper
import java.util.*

@Entity(tableName = ExamDBHelper.EXAM_TABLE_NAME)
data class Exam(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConst.COLUMN_ID)
    val id: Int,
    val courseId: String,
    val name: String,
    val credit: Float,
    val teachClass: String,
    val startDate: Date?,
    val endDate: Date?,
    @ColumnInfo(name = ExamDBHelper.COLUMN_DATE_RAW_TEXT)
    val dateRawText: String,
    val location: String,
    val property: String,
    val type: String
) {
    constructor(
        courseId: String, name: String, credit: Float, teachClass: String,
        startDate: Date?, endDate: Date?, dateRawText: String, location: String, property: String, type: String
    ) :
            this(DBConst.DEFAULT_ID, courseId, name, credit, teachClass, startDate, endDate, dateRawText, location, property, type)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Exam

        if (courseId != other.courseId) return false
        if (name != other.name) return false
        if (credit != other.credit) return false
        if (teachClass != other.teachClass) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (dateRawText != other.dateRawText) return false
        if (location != other.location) return false
        if (property != other.property) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + credit.hashCode()
        result = 31 * result + teachClass.hashCode()
        result = 31 * result + (startDate?.hashCode() ?: 0)
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + dateRawText.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + property.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}