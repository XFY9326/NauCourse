package tool.xfy9326.naucourse.beans

import java.io.Serializable

// 下一节课数据包
data class NextCourseBundle(
    // 是否课程数据为空
    val courseDataEmpty: Boolean,
    // 是否有下一节课
    val hasNextCourse: Boolean,
    // 是否在假期
    val inVacation: Boolean,
    // 课程数据包
    val courseBundle: CourseBundle?
) : Serializable {
    constructor(courseBundle: CourseBundle) : this(false, true, false, courseBundle)

    constructor(inVacation: Boolean) : this(false, false, inVacation, null)

    constructor() : this(true, false, false, null)
}