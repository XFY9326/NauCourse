package tool.xfy9326.naucourse.ui.models.activity

import tool.xfy9326.naucourse.providers.beans.jwc.Exam
import tool.xfy9326.naucourse.providers.info.base.InfoResult
import tool.xfy9326.naucourse.providers.info.methods.ExamInfo
import tool.xfy9326.naucourse.ui.models.base.BaseListViewModel

class ExamArrangeViewModel : BaseListViewModel<Exam>() {
    override suspend fun onUpdateData(isInit: Boolean): InfoResult<List<Exam>> {
        val data = ExamInfo.getInfo(loadCache = isInit)
        return if (data.isSuccess) {
            InfoResult(true, data.data!!.asList())
        } else {
            InfoResult(false, errorReason = data.errorReason)
        }
    }
}