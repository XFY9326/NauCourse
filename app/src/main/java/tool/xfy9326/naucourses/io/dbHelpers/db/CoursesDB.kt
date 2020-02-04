package tool.xfy9326.naucourses.io.dbHelpers.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.base.BaseDB
import tool.xfy9326.naucourses.io.dbHelpers.base.DBTypeConverter
import tool.xfy9326.naucourses.providers.contents.beans.jwc.*

class CoursesDB private constructor(context: Context) : BaseDB() {
    override val db = Room.databaseBuilder(
        context, CoursesDataBase::class.java,
        COURSES_DB_NAME
    ).build()

    companion object {
        @Volatile
        private lateinit var instance: CoursesDB

        private const val COURSES_DB_NAME = "Courses.db"
        private const val COURSES_DB_VERSION = 1

        fun initInstance(context: Context) = synchronized(this) {
            if (!Companion::instance.isInitialized) {
                instance =
                    CoursesDB(context)
            }
        }

        fun getInstance(): CoursesDB =
            instance
    }

    @Database(
        entities = [Course::class, CourseTime::class, Term::class, CourseScore::class, CourseHistory::class],
        version = COURSES_DB_VERSION,
        exportSchema = false
    )
    @TypeConverters(DBTypeConverter::class)
    abstract class CoursesDataBase : RoomDatabase() {
        abstract fun getCoursesDataDao(): CoursesDBHelper.CoursesDataDao

        abstract fun getCourseScoreDao(): CoursesDBHelper.CourseScoreDao

        abstract fun getCousesHistoryDao(): CoursesDBHelper.CourseHistoryDao
    }
}