package tool.xfy9326.naucourse.ui.models.activity

import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourse.providers.info.base.InfoResult
import tool.xfy9326.naucourse.providers.info.methods.LevelExamInfo
import tool.xfy9326.naucourse.ui.models.base.BaseListViewModel

class LevelExamViewModel : BaseListViewModel<LevelExam>() {
    override suspend fun onUpdateData(isInit: Boolean, forceUpdate: Boolean): InfoResult<List<LevelExam>> {
        val data = LevelExamInfo.getInfo(loadCache = isInit, forceRefresh = forceUpdate)
        return if (data.isSuccess) {
            InfoResult(true, data.data!!.asList())
        } else {
            InfoResult(false, errorReason = data.errorReason)
        }
    }
}