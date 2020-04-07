package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.ExamDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.Exam

@Dao
interface ExamDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putExam(vararg exam: Exam)

    @Query("select * from ${ExamDBHelper.EXAM_TABLE_NAME} order by ${ExamDBHelper.COLUMN_START_DATE} desc")
    fun getExam(): Array<Exam>

    @Query("delete from ${ExamDBHelper.EXAM_TABLE_NAME}")
    fun clearExam()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '${ExamDBHelper.EXAM_TABLE_NAME}'")
    fun clearIndex()
}