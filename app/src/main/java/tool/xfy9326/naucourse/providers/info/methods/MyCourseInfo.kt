package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.CourseScoreDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyCourse
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo

object MyCourseInfo : BaseSimpleContentInfo<Array<CourseScore>, Nothing>() {
    override fun loadSimpleStoredInfo(): Array<CourseScore>? = CourseScoreDBHelper.getCourseScores()

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Array<CourseScore>> = MyCourse.getContentData()

    override fun saveSimpleInfo(info: Array<CourseScore>) = CourseScoreDBHelper.putCourseScores(info)

    override fun clearSimpleStoredInfo() = CourseScoreDBHelper.clearAll()
}