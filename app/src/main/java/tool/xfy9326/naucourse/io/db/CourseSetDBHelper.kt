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
    const val COLUMN_WEEK_DAY = "weekDay"
    const val COLUMN_NAME = "name"

    override val db: CoursesDB.CoursesDataBase = CoursesDB.getDB()

    @Synchronized
    fun storeNewCourseSet(courseSet: CourseSet) = with(db.getCoursesDataDao()) {
        clearAll()
        putTerm(courseSet.term)
        putCourses(*courseSet.courses.toTypedArray())
        courseSet.courses.forEach {
            val putArray = it.timeSet.toTypedArray()
            putArray.forEach { courseTime ->
                courseTime.courseId = it.id
            }
            putCoursesTime(*putArray)
        }
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
        clearTerm()
        clearTableIndex(TERM_TABLE_NAME)
        clearCoursesTime()
        clearTableIndex(COURSES_TIME_TABLE_NAME)
        clearCourses()
    }

}