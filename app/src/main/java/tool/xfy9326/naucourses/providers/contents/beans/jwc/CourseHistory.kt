package tool.xfy9326.naucourses.providers.contents.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper

@Entity(tableName = CoursesDBHelper.COURSES_HISTORY_TABLE_NAME)
data class CourseHistory(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
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
                Constants.DB.DEFAULT_ID, courseId, name, credit, score, scoreRawText, creditWeight,
                term, courseProperty, academicProperty, type, notes
            )
}