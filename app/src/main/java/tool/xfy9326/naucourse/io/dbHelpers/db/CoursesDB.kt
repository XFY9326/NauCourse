package tool.xfy9326.naucourse.io.dbHelpers.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.dbHelpers.CoursesDBHelper
import tool.xfy9326.naucourse.io.dbHelpers.base.BaseDB
import tool.xfy9326.naucourse.io.dbHelpers.base.DBTypeConverter
import tool.xfy9326.naucourse.providers.beans.jwc.*

object CoursesDB : BaseDB() {
    private const val COURSES_DB_NAME = "Courses.db"
    private const val COURSES_DB_VERSION = 1

    override val db = Room.databaseBuilder(
        App.instance, CoursesDataBase::class.java,
        COURSES_DB_NAME
    ).build()

    @Database(
        entities = [Course::class, CourseTime::class, Term::class, CourseScore::class, CourseHistory::class],
        version = COURSES_DB_VERSION,
        exportSchema = false
    )
    @TypeConverters(DBTypeConverter::class)
    abstract class CoursesDataBase : RoomDatabase() {
        abstract fun getCoursesDataDao(): CoursesDBHelper.CoursesDataDao

        abstract fun getCourseScoreDao(): CoursesDBHelper.CourseScoreDao

        abstract fun getCoursesHistoryDao(): CoursesDBHelper.CoursesHistoryDao
    }
}