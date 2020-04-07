package tool.xfy9326.naucourse.providers.beans.jwc

import androidx.room.*
import tool.xfy9326.naucourse.io.db.CourseSetDBHelper
import java.io.Serializable

@Entity(
    tableName = CourseSetDBHelper.COURSES_TABLE_NAME,
    indices = [Index(value = [CourseSetDBHelper.COLUMN_NAME])]
)
data class Course(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = CourseSetDBHelper.COLUMN_NAME)
    val name: String,
    val teacher: String,
    val courseClass: String?,
    val teachClass: String,
    val credit: Float,
    val type: String,
    val property: String?,
    @Ignore var timeSet: HashSet<CourseTime>
) : Serializable {
    // For Room
    constructor(id: String, name: String, teacher: String, courseClass: String?, teachClass: String, credit: Float, type: String, property: String?) :
            this(id, name, teacher, courseClass, teachClass, credit, type, property, HashSet(0))

    // For Current Term
    constructor(
        id: String,
        name: String,
        teacher: String,
        courseClass: String,
        teachClass: String,
        credit: Float,
        type: String,
        timeSet: HashSet<CourseTime>
    ) : this(id, name, teacher, courseClass, teachClass, credit, type, null, timeSet)

    // For Next Term
    constructor(
        id: String,
        name: String,
        teacher: String,
        teachClass: String,
        credit: Float,
        type: String,
        property: String,
        timeSet: HashSet<CourseTime>
    ) : this(id, name, teacher, null, teachClass, credit, type, property, timeSet)

    init {
        if (credit < 0) {
            throw IllegalArgumentException("Course Credit Error! Credit: $credit")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Course

        if (id != other.id) return false
        if (name != other.name) return false
        if (teacher != other.teacher) return false
        if (courseClass != other.courseClass) return false
        if (teachClass != other.teachClass) return false
        if (credit != other.credit) return false
        if (type != other.type) return false
        if (property != other.property) return false
        if (timeSet != other.timeSet) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + teacher.hashCode()
        result = 31 * result + (courseClass?.hashCode() ?: 0)
        result = 31 * result + teachClass.hashCode()
        result = 31 * result + credit.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (property?.hashCode() ?: 0)
        result = 31 * result + timeSet.hashCode()
        return result
    }
}