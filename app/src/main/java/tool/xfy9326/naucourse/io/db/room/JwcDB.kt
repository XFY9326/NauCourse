package tool.xfy9326.naucourse.io.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tool.xfy9326.naucourse.io.db.ExamDBHelper
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

    private const val JWC_DB_VERSION = 2

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE ${ExamDBHelper.EXAM_TABLE_NAME}")
            database.execSQL("CREATE TABLE `${ExamDBHelper.EXAM_TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `courseId` TEXT NOT NULL, `name` TEXT NOT NULL, `credit` REAL NOT NULL, `teachClass` TEXT NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `dateRawText` TEXT NOT NULL, `location` TEXT NOT NULL, `property` TEXT NOT NULL, `type` TEXT NOT NULL)")
        }
    }

    override fun onBuildDB(builder: RoomDatabase.Builder<JwcDataBase>): JwcDataBase {
        builder.addMigrations(MIGRATION_1_2)
        return super.onBuildDB(builder)
    }

    @Database(
        entities = [TermDate::class, LevelExam::class, Exam::class],
        version = JWC_DB_VERSION
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