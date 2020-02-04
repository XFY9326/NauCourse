package tool.xfy9326.naucourses.io.dbHelpers

import androidx.room.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.db.AppDB
import tool.xfy9326.naucourses.providers.contents.beans.GeneralNews
import tool.xfy9326.naucourses.providers.contents.beans.jwc.Exam
import tool.xfy9326.naucourses.providers.contents.beans.jwc.TermDate

object AppDBHelper {
    const val NEWS_TABLE_NAME = "News"
    const val TERM_DATE_TABLE_NAME = "TermDate"
    const val LEVEL_EXAM_TABLE_NAME = "LevelExam"
    const val EXAM_TABLE_NAME = "Exam"
    const val COLUMN_POST_DATE = "postDate"
    const val COLUMN_HASH_ID = "hashId"
    const val COLUMN_START_DATE = "startDate"

    private val appDB = AppDB.getInstance().db

    fun putGeneralNewsSet(newsSet: Set<GeneralNews>) = with(appDB.getNewsDataDao()) {
        putNews(*newsSet.toTypedArray())
    }

    @Synchronized
    fun getGeneralNewsArray(): List<GeneralNews> = with(appDB.getNewsDataDao()) {
        clearOutOfDateNews(System.currentTimeMillis() - Constants.News.NEWS_STORE_DAY_LENGTH * 24 * 60 * 60 * 1000L)
        val newsArray = getAllNews()
        newsArray.forEach {
            if (it.postSource == GeneralNews.PostSource.UNKNOWN) {
                newsArray.remove(it)
                deleteNews(it)
            }
        }
        newsArray
    }

    fun clearGeneralNewsByType(type: GeneralNews.PostSource) = with(appDB.getNewsDataDao()) {
        clearNewsByType(type.name)
    }

    fun clearGeneralNewsSet() = with(appDB.getNewsDataDao()) {
        clearAllNews()
    }

    fun putTermDate(termDate: TermDate) = with(appDB.getTermDateDataDao()) {
        putTermDate(termDate)
    }

    @Synchronized
    fun getTermDate(): TermDate? = with(appDB.getTermDateDataDao()) {
        val termArr = getTermDate()
        if (termArr.isNotEmpty()) {
            termArr[0]
        } else {
            null
        }
    }

    @Synchronized
    fun clearTermDate() = with(AppDB.getInstance().db.getTermDateDataDao()) {
        clearTermDate()
        clearIndex()
    }

    fun putExam(examArr: Array<Exam>) = with(appDB.getExamDataDao()) {
        putExam(*examArr)
    }

    fun getExam(): Array<Exam> = with(appDB.getExamDataDao()) {
        getExam()
    }

    @Synchronized
    fun clearExam() = with(AppDB.getInstance().db.getExamDataDao()) {
        clearExam()
        clearIndex()
    }

    @Dao
    interface NewsDataDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun putNews(vararg news: GeneralNews)

        @Query("select * from $NEWS_TABLE_NAME order by $COLUMN_POST_DATE desc, $COLUMN_HASH_ID desc")
        fun getAllNews(): MutableList<GeneralNews>

        @Delete
        fun deleteNews(news: GeneralNews)

        @Query("delete from $NEWS_TABLE_NAME where $COLUMN_POST_DATE < :timeStamp")
        fun clearOutOfDateNews(timeStamp: Long)

        @Query("delete from $NEWS_TABLE_NAME where $COLUMN_POST_DATE < :type")
        fun clearNewsByType(type: String)

        @Query("delete from $NEWS_TABLE_NAME")
        fun clearAllNews()
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