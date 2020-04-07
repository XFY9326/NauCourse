package tool.xfy9326.naucourse.io.db

import tool.xfy9326.naucourse.io.db.base.BaseDBHelper
import tool.xfy9326.naucourse.io.db.room.CoursesDB
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory

object CourseHistoryDBHelper : BaseDBHelper<CoursesDB.CoursesDataBase>() {
    const val COURSES_HISTORY_TABLE_NAME = "CourseHistory"

    override val db: CoursesDB.CoursesDataBase = CoursesDB.getDB()

    @Synchronized
    fun putCourseHistoryArr(courseHistoryArr: Array<CourseHistory>) = with(db.getCoursesHistoryDao()) {
        putCourseHistory(*courseHistoryArr)
    }

    @Synchronized
    fun getCourseHistoryArr(): Array<CourseHistory> = with(db.getCoursesHistoryDao()) {
        getCourseHistory()
    }

    @Synchronized
    override fun clearAll() = with(db.getCoursesHistoryDao()) {
        clearCourseHistory()
        clearIndex()
    }
}