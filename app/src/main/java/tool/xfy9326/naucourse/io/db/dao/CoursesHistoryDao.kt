package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.CourseHistoryDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory

@Dao
interface CoursesHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCourseHistory(vararg courseHistory: CourseHistory)

    @Query("select * from ${CourseHistoryDBHelper.COURSES_HISTORY_TABLE_NAME}")
    fun getCourseHistory(): Array<CourseHistory>

    @Query("delete from ${CourseHistoryDBHelper.COURSES_HISTORY_TABLE_NAME}")
    fun clearCourseHistory()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '${CourseHistoryDBHelper.COURSES_HISTORY_TABLE_NAME}'")
    fun clearIndex()
}