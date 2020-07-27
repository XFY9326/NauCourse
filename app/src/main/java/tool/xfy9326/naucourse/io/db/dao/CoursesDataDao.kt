package tool.xfy9326.naucourse.io.db.dao

import androidx.room.*
import tool.xfy9326.naucourse.constants.DBConst
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
        putCoursesTime(*ArrayList<CourseTime>().apply {
            courseSet.courses.forEach {
                addAll(it.timeSet)
            }
        }.toTypedArray())
    }

    @Transaction
    fun getCourseSet(): CourseSet? {
        val dbCourseList = getCourses()
        val term = getTerm()
        return if (dbCourseList.isNotEmpty() && term.isNotEmpty()) {
            val courseSet = HashSet<Course>(dbCourseList.size)
            dbCourseList.forEach {
                it.timeSet = getCoursesTime(it.id).toHashSet()
                courseSet.add(it)
            }
            CourseSet(courseSet, term[0])
        } else {
            null
        }
    }

    @Transaction
    fun clearAll() {
        clearTerm()
        clearCoursesTime()
        clearCourses()
        clearTableIndex(CourseSetDBHelper.TERM_TABLE_NAME)
        clearTableIndex(CourseSetDBHelper.COURSES_TIME_TABLE_NAME)
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
    @Query("delete from ${DBConst.SQL_LITE_TABLE} where ${DBConst.COLUMN_NAME} = :tableName")
    fun clearTableIndex(tableName: String)
}