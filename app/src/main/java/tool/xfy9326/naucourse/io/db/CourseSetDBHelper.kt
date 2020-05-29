package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.CoursesDB
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet

object CourseSetDBHelper : BaseDBHelper<CoursesDB.CoursesDataBase>() {
    const val COURSES_TABLE_NAME = "Courses"
    const val TERM_TABLE_NAME = "Term"
    const val COURSES_TIME_TABLE_NAME = "CoursesTime"

    const val COLUMN_COURSE_ID = "courseId"
    const val COLUMN_WEEK_MODE = "weekMode"
    const val COLUMN_WEEK_DAY = "weekDay"
    const val COLUMN_WEEKS_STR = "weeksStr"
    const val COLUMN_COURSES_NUM_STR = "coursesNumStr"
    const val COLUMN_NAME = "name"

    override val db: CoursesDB.CoursesDataBase = CoursesDB.getDB()

    @Synchronized
    fun storeNewCourseSet(courseSet: CourseSet) = with(db.getCoursesDataDao()) {
        storeCourseSet(courseSet)
    }

    @Synchronized
    fun readCourseSet(): CourseSet? = with(db.getCoursesDataDao()) {
        val dbCourseList = getCourses()
        val term = getTerm()
        if (dbCourseList.isNotEmpty() && term.isNotEmpty()) {
            val courseSet = HashSet<Course>(dbCourseList.size)
            dbCourseList.forEach {
                it.timeSet = getCoursesTime(it.id).toHashSet()
                courseSet.add(it)
            }
            CourseSet(courseSet, term[0])
        } else {
            null
        }
    }

    @Synchronized
    override fun clearAll() = with(db.getCoursesDataDao()) {
        clearAll()
    }

}