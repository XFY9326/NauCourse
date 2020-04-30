package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.JwcDB
import tool.xfy9326.naucourse.providers.beans.jwc.Exam

object ExamDBHelper : BaseDBHelper<JwcDB.JwcDataBase>() {
    const val EXAM_TABLE_NAME = "Exam"
    const val COLUMN_DATE_RAW_TEXT = "dateRawText"

    override val db: JwcDB.JwcDataBase = JwcDB.getDB()

    @Synchronized
    fun putExam(examArr: Array<Exam>) = with(db.getExamDataDao()) {
        clearAll()
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