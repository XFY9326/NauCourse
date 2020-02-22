package tool.xfy9326.naucourses.io.dbHelpers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.db.JwcDB
import tool.xfy9326.naucourses.providers.beans.jwc.Exam
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate

object JwcDBHelper {
    const val TERM_DATE_TABLE_NAME = "TermDate"
    const val LEVEL_EXAM_TABLE_NAME = "LevelExam"
    const val EXAM_TABLE_NAME = "Exam"
    const val COLUMN_START_DATE = "startDate"

    private val jwcDB = JwcDB.getInstance().db

    fun putTermDate(termDate: TermDate) = with(jwcDB.getTermDateDataDao()) {
        putTermDate(termDate)
    }

    @Synchronized
    fun getTermDate(): TermDate? = with(jwcDB.getTermDateDataDao()) {
        val termArr = getTermDate()
        if (termArr.isNotEmpty()) {
            termArr[0]
        } else {
            null
        }
    }

    @Synchronized
    fun clearTermDate() = with(jwcDB.getTermDateDataDao()) {
        clearTermDate()
        clearIndex()
    }

    fun putExam(examArr: Array<Exam>) = with(jwcDB.getExamDataDao()) {
        putExam(*examArr)
    }

    fun getExam(): Array<Exam> = with(jwcDB.getExamDataDao()) {
        getExam()
    }

    @Synchronized
    fun clearExam() = with(jwcDB.getExamDataDao()) {
        clearExam()
        clearIndex()
    }

    @Dao
    interface TermDateDataDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putTermDate(termDate: TermDate)

        @Query("select * from $TERM_DATE_TABLE_NAME limit 1")
        fun getTermDate(): Array<TermDate>

        @Query("delete from $TERM_DATE_TABLE_NAME")
        fun clearTermDate()

        @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '$TERM_DATE_TABLE_NAME'")
        fun clearIndex()
    }

    @Dao
    interface ExamDataDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putExam(vararg exam: Exam)

        @Query("select * from $EXAM_TABLE_NAME order by $COLUMN_START_DATE desc")
        fun getExam(): Array<Exam>

        @Query("delete from $EXAM_TABLE_NAME")
        fun clearExam()

        @Query("delete from ${Constants.DB.SQL_LITE_TABLE} where ${Constants.DB.COLUMN_NAME} = '$EXAM_TABLE_NAME'")
        fun clearIndex()
    }
}