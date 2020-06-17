package tool.xfy9326.naucourse.io.db.dao

import androidx.room.*
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.utils.courses.CourseStyleUtils

@Dao
interface CoursesCellStyleDao {
    @Transaction
    fun getFixedCourseCellStyle(courseSet: CourseSet) = CourseStyleUtils.asyncCellStyle(courseSet, getCourseCellStyle())

    @Transaction
    fun setCourseCellStyle(styles: Array<CourseCellStyle>) {
        clearCourseCellStyle()
        putCourseCellStyle(*styles)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCourseCellStyle(vararg courseCellStyle: CourseCellStyle)

    @Query("select * from ${CourseCellStyleDBHelper.COURSES_CELL_STYLE_TABLE_NAME}")
    fun getCourseCellStyle(): Array<CourseCellStyle>

    @Query("delete from ${CourseCellStyleDBHelper.COURSES_CELL_STYLE_TABLE_NAME}")
    fun clearCourseCellStyle()
}