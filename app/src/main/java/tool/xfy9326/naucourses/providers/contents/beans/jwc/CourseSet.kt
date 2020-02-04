package tool.xfy9326.naucourses.providers.contents.beans.jwc

data class CourseSet(
    val courses: Set<Course>,
    val term: Term
) {
    init {
        if (courses.isEmpty()) {
            throw IllegalArgumentException("Course Set Is Empty!")
        }
    }
}