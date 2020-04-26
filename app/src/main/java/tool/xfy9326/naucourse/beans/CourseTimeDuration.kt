package tool.xfy9326.naucourse.beans

// 课程时间间隔（用于课表生成）
data class CourseTimeDuration(
    // 开始时间
    val startTime: Int,
    // 持续长度，最小应该为1
    val durationLength: Int
)