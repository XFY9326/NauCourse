package tool.xfy9326.naucourses.providers.contents.beans.jwc

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StudentLearningProcess

        if (courseType != other.courseType) return false
        if (title != other.title) return false
        if (progress != other.progress) return false
        if (subjects != other.subjects) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseType.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + progress
        result = 31 * result + subjects.hashCode()
        return result
    }

}