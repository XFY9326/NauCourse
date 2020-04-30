package tool.xfy9326.naucourse.io.db.room

import androidx.room.Database
import androidx.room.TypeConverters
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.db.CourseSetDBHelper
import tool.xfy9326.naucourse.io.db.base.BaseDB
import tool.xfy9326.naucourse.io.db.base.DBTypeConverter
import tool.xfy9326.naucourse.io.db.dao.CourseScoreDao
import tool.xfy9326.naucourse.io.db.dao.CoursesCellStyleDao
import tool.xfy9326.naucourse.io.db.dao.CoursesDataDao
import tool.xfy9326.naucourse.io.db.dao.CoursesHistoryDao
import tool.xfy9326.naucourse.providers.beans.jwc.*

object CoursesDB : BaseDB<CoursesDB.CoursesDataBase>() {
    override val dbName: String = "Courses.db"
    override val dbClass: Class<CoursesDataBase> = CoursesDataBase::class.java

    private const val COURSES_DB_VERSION = 1

    @Database(
        entities = [Course::class, CourseTime::class, Term::class, CourseScore::class, CourseHistory::class, CourseCellStyle::class],
        version = COURSES_DB_VERSION
    )
    @TypeConverters(DBTypeConverter::class)
    abstract class CoursesDataBase : DB() {
        abstract fun getCoursesDataDao(): CoursesDataDao

        abstract fun getCourseScoreDao(): CourseScoreDao

        abstract fun getCoursesHistoryDao(): CoursesHistoryDao

        abstract fun getCoursesCellStyleDao(): CoursesCellStyleDao

        override fun clearAll() {
            getCoursesDataDao().clearTableIndex(CourseSetDBHelper.TERM_TABLE_NAME)
            getCoursesDataDao().clearTableIndex(CourseSetDBHelper.COURSES_TIME_TABLE_NAME)
            getCoursesHistoryDao().clearIndex()
            super.clearAll()
        }
    }
}