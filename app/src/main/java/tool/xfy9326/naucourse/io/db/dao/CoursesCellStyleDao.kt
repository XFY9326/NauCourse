package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper

@Dao
interface CoursesCellStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCourseCellStyle(vararg courseCellStyle: CourseCellStyle)

    @Insert
    fun addCourseCellStyle(vararg courseCellStyle: CourseCellStyle)

    @Query("select * from ${CourseCellStyleDBHelper.COURSES_CELL_STYLE_TABLE_NAME}")
    fun getCourseCellStyle(): Array<CourseCellStyle>

    @Query("delete from ${CourseCellStyleDBHelper.COURSES_CELL_STYLE_TABLE_NAME}")
    fun clearCourseCellStyle()
}