package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.constants.DBConst
import tool.xfy9326.naucourse.io.db.LevelExamDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam

@Dao
interface LevelExamDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putLevelExam(vararg levelExam: LevelExam)

    @Query("select * from ${LevelExamDBHelper.LEVEL_EXAM_TABLE_NAME}")
    fun getLevelExam(): Array<LevelExam>

    @Query("delete from ${LevelExamDBHelper.LEVEL_EXAM_TABLE_NAME}")
    fun clearLevelExam()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${DBConst.SQL_LITE_TABLE} where ${DBConst.COLUMN_NAME} = '${LevelExamDBHelper.LEVEL_EXAM_TABLE_NAME}'")
    fun clearIndex()
}