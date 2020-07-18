package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.naucourse.constants.DBConst
import tool.xfy9326.naucourse.io.db.LevelExamDBHelper

@Entity(tableName = LevelExamDBHelper.LEVEL_EXAM_TABLE_NAME)
data class LevelExam(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConst.COLUMN_ID)
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
            this(DBConst.DEFAULT_ID, type, name, grade1, grade2, term, ticketNum, certificateNum, notes)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LevelExam

        if (type != other.type) return false
        if (name != other.name) return false
        if (grade1 != other.grade1) return false
        if (grade2 != other.grade2) return false
        if (term != other.term) return false
        if (ticketNum != other.ticketNum) return false
        if (certificateNum != other.certificateNum) return false
        if (notes != other.notes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (grade1?.hashCode() ?: 0)
        result = 31 * result + grade2.hashCode()
        result = 31 * result + term.hashCode()
        result = 31 * result + ticketNum.hashCode()
        result = 31 * result + certificateNum.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }
}