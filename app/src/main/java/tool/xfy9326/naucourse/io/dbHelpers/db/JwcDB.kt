package tool.xfy9326.naucourse.io.dbHelpers.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.dbHelpers.JwcDBHelper
import tool.xfy9326.naucourse.io.dbHelpers.base.BaseDB
import tool.xfy9326.naucourse.io.dbHelpers.base.DBTypeConverter
import tool.xfy9326.naucourse.providers.beans.jwc.Exam
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate

object JwcDB : BaseDB() {
    private const val JWC_DB_NAME = "Jwc.db"
    private const val JWC_DB_VERSION = 1

    override val db = Room.databaseBuilder(
        App.instance, JwcDataBase::class.java,
        JWC_DB_NAME
    ).build()

    @Database(
        entities = [TermDate::class, LevelExam::class, Exam::class],
        version = JWC_DB_VERSION,
        exportSchema = false
    )
    @TypeConverters(DBTypeConverter::class)
    abstract class JwcDataBase : RoomDatabase() {
        abstract fun getTermDateDataDao(): JwcDBHelper.TermDateDataDao

        abstract fun getExamDataDao(): JwcDBHelper.ExamDataDao
    }
}