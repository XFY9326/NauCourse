package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.LevelExamDBHelper

@Entity(tableName = LevelExamDBHelper.LEVEL_EXAM_TABLE_NAME)
data class LevelExam(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
    val id: Int,
    val type: String,
    val name: String,
    val grade1: Float?,
    val grade2: String,
    val term: Term,
    val ticketNum: String,
    val certificateNum: String,
    val notes: String
) {
    constructor(type: String, name: String, grade1: Float?, grade2: String, term: Term, ticketNum: String, certificateNum: String, notes: String) :
            this(Constants.DB.DEFAULT_ID, type, name, grade1, grade2, term, ticketNum, certificateNum, notes)
}