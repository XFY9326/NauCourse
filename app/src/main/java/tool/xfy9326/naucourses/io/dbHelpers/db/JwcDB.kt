package tool.xfy9326.naucourses.io.dbHelpers.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tool.xfy9326.naucourses.io.dbHelpers.JwcDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.base.BaseDB
import tool.xfy9326.naucourses.io.dbHelpers.base.DBTypeConverter
import tool.xfy9326.naucourses.providers.beans.jwc.Exam
import tool.xfy9326.naucourses.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate

class JwcDB private constructor(context: Context) : BaseDB() {
    override val db = Room.databaseBuilder(
        context, JwcDataBase::class.java,
        JWC_DB_NAME
    ).build()

    companion object {
        @Volatile
        private lateinit var instance: JwcDB

        private const val JWC_DB_NAME = "Jwc.db"
        private const val JWC_DB_VERSION = 1

        fun initInstance(context: Context) = synchronized(this) {
            if (!Companion::instance.isInitialized) {
                instance = JwcDB(context)
            }
        }

        fun getInstance(): JwcDB = instance
    }

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