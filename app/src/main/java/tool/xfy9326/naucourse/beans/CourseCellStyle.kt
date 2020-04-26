package tool.xfy9326.naucourse.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import java.io.Serializable

// 课程格风格
@Entity(tableName = CourseCellStyleDBHelper.COURSES_CELL_STYLE_TABLE_NAME)
data class CourseCellStyle(
    //课程ID
    @PrimaryKey
    val courseId: String,
    // 课程颜色
    var color: Int
) : Serializable