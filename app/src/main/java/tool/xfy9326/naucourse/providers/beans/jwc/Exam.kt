package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.dbHelpers.JwcDBHelper
import java.util.*

@Entity(tableName = JwcDBHelper.EXAM_TABLE_NAME)
data class Exam(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
    val id: Int,
    val courseId: String,
    val name: String,
    val credit: Float,
    val teachClass: String,
    @ColumnInfo(name = JwcDBHelper.COLUMN_START_DATE)
    val startDate: Date,
    val endDate: Date,
    val location: String,
    val property: String,
    val type: String
) {
    constructor(
        courseId: String, name: String, credit: Float, teachClass: String,
        startDate: Date, endDate: Date, location: String, property: String, type: String
    ) :
            this(Constants.DB.DEFAULT_ID, courseId, name, credit, teachClass, startDate, endDate, location, property, type)
}