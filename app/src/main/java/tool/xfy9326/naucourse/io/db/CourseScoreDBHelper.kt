package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.CoursesDB
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore

object CourseScoreDBHelper : BaseDBHelper<CoursesDB.CoursesDataBase>() {
    const val COURSES_SCORE_TABLE_NAME = "CoursesScore"
    const val COLUMN_COURSE_ID = "courseId"

    override val db: CoursesDB.CoursesDataBase = CoursesDB.getDB()

    @Synchronized
    fun putCourseScores(courseScoreSet: Array<CourseScore>) = with(db.getCourseScoreDao()) {
        clearAll()
        putCourseScore(*courseScoreSet)
    }

    @Synchronized
    fun getCourseScores(): Array<CourseScore> = with(db.getCourseScoreDao()) {
        getCourseScores()
    }

    @Synchronized
    override fun clearAll() = with(db.getCourseScoreDao()) {
        clearCourseScores()
    }
}