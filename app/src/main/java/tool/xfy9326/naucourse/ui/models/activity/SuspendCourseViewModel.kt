package tool.xfy9326.naucourse.ui.models.activity

import tool.xfy9326.naucourse.providers.beans.jwc.SuspendCourse
import tool.xfy9326.naucourse.providers.info.base.InfoResult
import tool.xfy9326.naucourse.providers.info.methods.SuspendCourseInfo
import tool.xfy9326.naucourse.ui.models.base.BaseListViewModel

class SuspendCourseViewModel : BaseListViewModel<SuspendCourse>() {
    override suspend fun onUpdateData(isInit: Boolean, forceUpdate: Boolean): InfoResult<List<SuspendCourse>> {
        val data = SuspendCourseInfo.getInfo(loadCache = isInit, forceRefresh = forceUpdate)
        return if (data.isSuccess) {
            InfoResult(true, data.data!!.asList())
        } else {
            InfoResult(false, errorReason = data.errorReason)
        }
    }
}