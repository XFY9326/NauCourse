package tool.xfy9326.naucourse.beans

import android.os.Bundle
import java.io.Serializable

data class NextCourseNotification(
    val courseName: String,
    val courseTeacher: String,
    val courseLocation: String,
    val courseStartDateTime: Long,
    val courseEndDateTime: Long
) : Serializable {

    companion object {
        private const val EXTRA_NEXT_COURSE_NAME = "EXTRA_NEXT_COURSE_NAME"
        private const val EXTRA_NEXT_COURSE_TEACHER = "EXTRA_NEXT_COURSE_TEACHER"
        private const val EXTRA_NEXT_COURSE_LOCATION = "EXTRA_NEXT_COURSE_LOCATION"
        private const val EXTRA_NEXT_COURSE_START_TIME = "EXTRA_NEXT_COURSE_START_TIME"
        private const val EXTRA_NEXT_COURSE_END_TIME = "EXTRA_NEXT_COURSE_END_TIME"

        fun fromBundle(bundle: Bundle) = NextCourseNotification(
            bundle.getString(EXTRA_NEXT_COURSE_NAME, null)!!,
            bundle.getString(EXTRA_NEXT_COURSE_TEACHER, null)!!,
            bundle.getString(EXTRA_NEXT_COURSE_LOCATION, null)!!,
            bundle.getLong(EXTRA_NEXT_COURSE_START_TIME),
            bundle.getLong(EXTRA_NEXT_COURSE_END_TIME)
        )
    }

    fun toBundle() = Bundle().apply {
        putString(EXTRA_NEXT_COURSE_NAME, courseName)
        putString(EXTRA_NEXT_COURSE_TEACHER, courseTeacher)
        putString(EXTRA_NEXT_COURSE_LOCATION, courseLocation)
        putLong(EXTRA_NEXT_COURSE_START_TIME, courseStartDateTime)
        putLong(EXTRA_NEXT_COURSE_END_TIME, courseEndDateTime)
    }
}