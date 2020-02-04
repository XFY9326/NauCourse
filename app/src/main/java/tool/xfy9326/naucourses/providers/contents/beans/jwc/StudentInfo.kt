package tool.xfy9326.naucourses.providers.contents.beans.jwc

data class StudentInfo(
    val personalInfo: StudentPersonalInfo,
    val learningProcess: Array<StudentLearningProcess>,
    val creditInfo: LinkedHashMap<String, Float>,
    val rankingInfo: LinkedHashMap<String, String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StudentInfo

        if (personalInfo != other.personalInfo) return false
        if (!learningProcess.contentEquals(other.learningProcess)) return false
        if (creditInfo != other.creditInfo) return false
        if (rankingInfo != other.rankingInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = personalInfo.hashCode()
        result = 31 * result + learningProcess.contentHashCode()
        result = 31 * result + creditInfo.hashCode()
        result = 31 * result + rankingInfo.hashCode()
        return result
    }
}