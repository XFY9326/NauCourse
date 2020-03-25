package tool.xfy9326.naucourse.providers.beans.jwc

data class StudentLearningProcess(
    val courseType: CourseType,
    val title: String,
    val progress: Int,
    val subjects: LinkedHashMap<SubjectType, Float>
) {
    enum class CourseType {
        COMPULSORY,
        MAJOR_SELECTIVE,
        OPTIONAL,
        PRACTICAL
    }

    enum class SubjectType {
        TARGET,
        REVISED,
        BONUS,
        BALANCE
    }
}