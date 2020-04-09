package tool.xfy9326.naucourse.beans

import java.io.Serializable

data class NextCourseBundle(
    val courseDataEmpty: Boolean,
    val hasNextCourse: Boolean,
    val inVacation: Boolean,
    val courseBundle: CourseBundle?
) : Serializable {
    constructor(courseBundle: CourseBundle) : this(false, true, false, courseBundle)

    constructor(inVacation: Boolean) : this(false, false, inVacation, null)

    constructor() : this(true, false, false, null)
}