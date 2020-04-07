package tool.xfy9326.naucourse.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import java.io.Serializable

@Entity(tableName = CourseCellStyleDBHelper.COURSES_CELL_STYLE_TABLE_NAME)
data class CourseCellStyle(
    @PrimaryKey
    val courseId: String,
    var color: Int
) : Serializable