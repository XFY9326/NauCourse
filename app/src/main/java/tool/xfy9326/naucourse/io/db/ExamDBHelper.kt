package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.JwcDB
import tool.xfy9326.naucourse.providers.beans.jwc.Exam

object ExamDBHelper : BaseDBHelper<JwcDB.JwcDataBase>() {
    const val LEVEL_EXAM_TABLE_NAME = "LevelExam"
    const val EXAM_TABLE_NAME = "Exam"
    const val COLUMN_START_DATE = "startDate"

    override val db: JwcDB.JwcDataBase = JwcDB.getDB()

    @Synchronized
    fun putExam(examArr: Array<Exam>) = with(db.getExamDataDao()) {
        putExam(*examArr)
    }

    @Synchronized
    fun getExam(): Array<Exam> = with(db.getExamDataDao()) {
        getExam()
    }

    @Synchronized
    override fun clearAll() = with(db.getExamDataDao()) {
        clearExam()
        clearIndex()
    }
}