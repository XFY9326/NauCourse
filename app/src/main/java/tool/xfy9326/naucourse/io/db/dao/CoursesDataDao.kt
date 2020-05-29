package tool.xfy9326.naucourse.io.db.dao

import androidx.room.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.CourseSetDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.providers.beans.jwc.Term

@Dao
interface CoursesDataDao {
    @Transaction
    fun storeCourseSet(courseSet: CourseSet) {
        clearAll()
        putTerm(courseSet.term)
        putCourses(*courseSet.courses.toTypedArray())
        courseSet.courses.forEach {
            putCoursesTime(*it.timeSet.toTypedArray())
        }
    }

    @Transaction
    fun clearAll() {
        clearTerm()
        clearTableIndex(CourseSetDBHelper.TERM_TABLE_NAME)
        clearCoursesTime()
        clearTableIndex(CourseSetDBHelper.COURSES_TIME_TABLE_NAME)
        clearCourses()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putTerm(term: Term)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCourses(vararg course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCoursesTime(vararg courseTime: CourseTime)

    @Query("select * from ${CourseSetDBHelper.TERM_TABLE_NAME} limit 1")
    fun getTerm(): Array<Term>

    @Query("select * from ${CourseSetDBHelper.COURSES_TABLE_NAME}")
    fun getCourses(): Array<Course>

    @Query("select * from ${CourseSetDBHelper.COURSES_TIME_TABLE_NAME} where ${CourseSetDBHelper.COLUMN_COURSE_ID} = :courseId")
    fun getCoursesTime(courseId: String): Array<CourseTime>

    @Query("delete from ${CourseSetDBHelper.COURSES_TABLE_NAME}")
    fun clearCourses()

    @Query("delete from ${CourseSetDBHelper.TERM_TABLE_NAME}")
    fun clearTerm()

    @Query("delete from ${CourseSetDBHelper.COURSES_TIME_TABLE_NAME}")
    fun clearCoursesTime()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = :tableName")
    fun clearTableIndex(tableName: String)
}