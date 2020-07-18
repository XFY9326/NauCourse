package tool.xfy9326.naucourse.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourse.constants.DBConst
import tool.xfy9326.naucourse.io.db.TermDateDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate

@Dao
interface TermDateDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putTermDate(termDate: TermDate)

    @Query("select * from ${TermDateDBHelper.TERM_DATE_TABLE_NAME} limit 1")
    fun getTermDate(): Array<TermDate>

    @Query("delete from ${TermDateDBHelper.TERM_DATE_TABLE_NAME}")
    fun clearTermDate()

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("delete from ${DBConst.SQL_LITE_TABLE} where ${DBConst.COLUMN_NAME} = '${TermDateDBHelper.TERM_DATE_TABLE_NAME}'")
    fun clearIndex()
}