package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.CoursesDB
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet

object CourseCellStyleDBHelper : BaseDBHelper<CoursesDB.CoursesDataBase>() {
    const val COURSES_CELL_STYLE_TABLE_NAME = "CoursesCellStyle"

    override val db: CoursesDB.CoursesDataBase = CoursesDB.getDB()

    @Synchronized
    fun saveCourseCellStyle(styles: Array<CourseCellStyle>) = db.getCoursesCellStyleDao().setCourseCellStyle(styles)

    @Synchronized
    fun loadCourseCellStyle(courseSet: CourseSet) = db.getCoursesCellStyleDao().getFixedCourseCellStyle(courseSet)

    @Synchronized
    override fun clearAll() = with(db.getCoursesCellStyleDao()) {
        clearCourseCellStyle()
    }
}