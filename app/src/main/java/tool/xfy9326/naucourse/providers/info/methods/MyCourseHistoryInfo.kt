package tool.xfy9326.naucourse.providers.info.methods

import tool.xfy9326.naucourse.io.db.CourseHistoryDBHelper
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.MyCourseHistory
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo

object MyCourseHistoryInfo : BaseSimpleContentInfo<Array<CourseHistory>, Nothing>() {
    override fun loadSimpleStoredInfo(): Array<CourseHistory>? = CourseHistoryDBHelper.getCourseHistoryArr()

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Array<CourseHistory>> = MyCourseHistory.getContentData()

    override fun saveSimpleInfo(info: Array<CourseHistory>) = CourseHistoryDBHelper.putCourseHistoryArr(info)

    override fun clearSimpleStoredInfo() = CourseHistoryDBHelper.clearAll()
}