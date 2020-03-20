package tool.xfy9326.naucourses.io.dbHelpers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.db.CoursesDB
import tool.xfy9326.naucourses.providers.beans.jwc.*

object CoursesDBHelper {
    const val COURSES_TABLE_NAME = "Courses"
    const val TERM_TABLE_NAME = "Term"
    const val COURSES_TIME_TABLE_NAME = "CoursesTime"
    const val COURSES_SCORE_TABLE_NAME = "CoursesScore"
    const val COURSES_HISTORY_TABLE_NAME = "CourseHistory"

    const val COLUMN_COURSE_ID = "courseId"
    const val COLUMN_WEEK_DAY = "weekDay"

    private val courseDB = CoursesDB.db

    @Synchronized
    fun storeNewCourseSet(courseSet: CourseSet) = with(courseDB.getCoursesDataDao()) {
        clearAllCoursesInfo()
        putTerm(courseSet.term)
        putCourses(*courseSet.courses.toTypedArray())
        courseSet.courses.forEach {
            val putArray = it.timeSet.toTypedArray()
            putArray.forEach { courseTime ->
                courseTime.courseId = it.id
            }
            putCoursesTime(*putArray)
        }
    }

    @Synchronized
    fun readCourseSet(): CourseSet? = with(courseDB.getCoursesDataDao()) {
        val dbCourseList = getCourses()
        val term = getTerm()
        if (dbCourseList.isNotEmpty() && term.isNotEmpty()) {
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

    @Synchronized
    fun clearAllCoursesInfo() = with(courseDB.getCoursesDataDao()) {
        clearTerm()
        clearTableIndex(TERM_TABLE_NAME)
        clearCoursesTime()
        clearTableIndex(COURSES_TIME_TABLE_NAME)
        clearCourses()
    }


    fun putCourseScores(courseScoreSet: Array<CourseScore>) = with(courseDB.getCourseScoreDao()) {
        putCourseScore(*courseScoreSet)
    }

    fun getCourseScores(): Array<CourseScore> = with(courseDB.getCourseScoreDao()) {
        getCourseScores()
    }

    fun clearCourseScore() = with(courseDB.getCourseScoreDao()) {
        clearCourseScores()
    }


    fun putCourseHistoryArr(courseHistoryArr: Array<CourseHistory>) = with(courseDB.getCoursesHistoryDao()) {
        putCourseHistory(*courseHistoryArr)
    }

    fun getCourseHistoryArr(): Array<CourseHistory> = with(courseDB.getCoursesHistoryDao()) {
        getCourseHistory()
    }

    @Synchronized
    fun clearAllCourseHistory() = with(courseDB.getCoursesHistoryDao()) {
        clearCourseHistory()
        clearIndex()
    }

    fun clearAll() {
        clearCourseScore()
        clearAllCourseHistory()
        clearAllCoursesInfo()
    }

    @Dao
    interface CoursesDataDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putTerm(term: Term)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putCourses(vararg course: Course)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putCoursesTime(vararg courseTime: CourseTime)

        @Query("select * from $TERM_TABLE_NAME limit 1")
        fun getTerm(): Array<Term>

        @Query("select * from $COURSES_TABLE_NAME")
        fun getCourses(): Array<Course>

        @Query("select * from $COURSES_TIME_TABLE_NAME where $COLUMN_COURSE_ID = :courseId")
        fun getCoursesTime(courseId: String): Array<CourseTime>

        @Query("delete from $COURSES_TABLE_NAME")
        fun clearCourses()

        @Query("delete from $TERM_TABLE_NAME")
        fun clearTerm()

        @Query("delete from $COURSES_TIME_TABLE_NAME")
        fun clearCoursesTime()

        @Suppress("AndroidUnresolvedRoomSqlReference")
        @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = :tableName")
        fun clearTableIndex(tableName: String)
    }

    @Dao
    interface CourseScoreDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putCourseScore(vararg courseScore: CourseScore)

        @Query("select * from $COURSES_SCORE_TABLE_NAME")
        fun getCourseScores(): Array<CourseScore>

        @Query("delete from $COURSES_SCORE_TABLE_NAME")
        fun clearCourseScores()
    }

    @Dao
    interface CoursesHistoryDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putCourseHistory(vararg courseHistory: CourseHistory)

        @Query("select * from $COURSES_HISTORY_TABLE_NAME")
        fun getCourseHistory(): Array<CourseHistory>

        @Query("delete from $COURSES_HISTORY_TABLE_NAME")
        fun clearCourseHistory()

        @Suppress("AndroidUnresolvedRoomSqlReference")
        @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '$COURSES_HISTORY_TABLE_NAME'")
        fun clearIndex()
    }
}