package tool.xfy9326.naucourses.providers.beans.jwc

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper
import java.io.Serializable

@Entity(tableName = CoursesDBHelper.COURSES_TABLE_NAME)
data class Course(
    @PrimaryKey
    val id: String,
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

        return true
    }

    override fun hashCode(): Int = id.hashCode()
}