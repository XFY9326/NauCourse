package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.JwcDB
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam

object LevelExamDBHelper : BaseDBHelper<JwcDB.JwcDataBase>() {
    const val LEVEL_EXAM_TABLE_NAME = "LevelExam"

    override val db: JwcDB.JwcDataBase = JwcDB.getDB()

    @Synchronized
    fun putLevelExam(examArr: Array<LevelExam>) = with(db.getLevelExamDataDao()) {
        clearAll()
        putLevelExam(*examArr)
    }

    @Synchronized
    fun getLevelExam(): Array<LevelExam> = with(db.getLevelExamDataDao()) {
        getLevelExam()
    }

    @Synchronized
    override fun clearAll() = with(db.getLevelExamDataDao()) {
        clearLevelExam()
        clearIndex()
    }
}