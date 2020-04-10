package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.JwcDB
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate

object TermDateDBHelper : BaseDBHelper<JwcDB.JwcDataBase>() {
    const val TERM_DATE_TABLE_NAME = "TermDate"

    override val db: JwcDB.JwcDataBase = JwcDB.getDB()

    @Synchronized
    fun putTermDate(termDate: TermDate) = with(db.getTermDateDataDao()) {
        clearAll()
        putTermDate(termDate)
    }

    @Synchronized
    fun getTermDate(): TermDate? = with(db.getTermDateDataDao()) {
        val termArr = getTermDate()
        if (termArr.isNotEmpty()) {
            termArr[0]
        } else {
            null
        }
    }

    @Synchronized
    override fun clearAll() = with(db.getTermDateDataDao()) {
        clearTermDate()
        clearIndex()
    }
}