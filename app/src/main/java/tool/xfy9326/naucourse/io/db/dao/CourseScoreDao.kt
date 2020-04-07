package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.io.db.CourseScoreDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore

@Dao
interface CourseScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCourseScore(vararg courseScore: CourseScore)

    @Query("select * from ${CourseScoreDBHelper.COURSES_SCORE_TABLE_NAME}")
    fun getCourseScores(): Array<CourseScore>

    @Query("delete from ${CourseScoreDBHelper.COURSES_SCORE_TABLE_NAME}")
    fun clearCourseScores()
}