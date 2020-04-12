package tool.xfy9326.naucourse.io.db.room

import androidx.room.Database
import androidx.room.TypeConverters
import tool.xfy9326.naucourse.io.db.base.BaseDB
import tool.xfy9326.naucourse.io.db.base.DBTypeConverter
import tool.xfy9326.naucourse.io.db.dao.ExamDataDao
import tool.xfy9326.naucourse.io.db.dao.LevelExamDataDao
import tool.xfy9326.naucourse.io.db.dao.TermDateDataDao
import tool.xfy9326.naucourse.providers.beans.jwc.Exam
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate

object JwcDB : BaseDB<JwcDB.JwcDataBase>() {
    override val dbName: String = "Jwc.db"
    override val dbClass: Class<JwcDataBase> = JwcDataBase::class.java

    private const val JWC_DB_VERSION = 1

    @Database(
        entities = [TermDate::class, LevelExam::class, Exam::class],
        version = JWC_DB_VERSION,
        exportSchema = false
    )
    @TypeConverters(DBTypeConverter::class)
    abstract class JwcDataBase : DB() {
        abstract fun getTermDateDataDao(): TermDateDataDao

        abstract fun getExamDataDao(): ExamDataDao

        abstract fun getLevelExamDataDao(): LevelExamDataDao

        override fun clearAll() {
            getTermDateDataDao().clearIndex()
            getLevelExamDataDao().clearIndex()
            getExamDataDao().clearIndex()
            super.clearAll()
        }
    }
}