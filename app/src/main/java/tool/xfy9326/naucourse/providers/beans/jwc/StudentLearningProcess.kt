package tool.xfy9326.naucourse.providers.beans.jwc

data class StudentLearningProcess(
    val courseType: CourseType,
    val title: String,
    val progress: Int,
    val subjects: LinkedHashMap<SubjectType, Float>
) {
    enum class CourseType {
        // 必修课
        COMPULSORY,

        // 专选课
        MAJOR_SELECTIVE,

        // 任选课
        OPTIONAL,

        // 时间课
        PRACTICAL
    }

    enum class SubjectType {
        // 目标学分
        TARGET,

        // 已修学分
        REVISED,

        // 奖励学分
        BONUS,

        // 学分差额
        BALANCE
    }
}