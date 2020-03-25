package tool.xfy9326.naucourse.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.debug.LogUtils

class UserInfoViewModel : BaseViewModel() {
    val studentInfo = MutableLiveData<StudentInfo>()

    override fun onInitView(isRestored: Boolean) {
        if (!isRestored) {
            updatePersonalInfo(true)
        }
        updatePersonalInfo()
    }

    private fun updatePersonalInfo(initLoad: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val personalInfo = PersonalInfo.getInfo(loadCache = initLoad)
            if (personalInfo.isSuccess) {
                studentInfo.postValue(personalInfo.data!!)
            } else {
                LogUtils.d<UserInfoViewModel>("PersonalInfo Error: ${personalInfo.errorReason}")
            }
        }
    }
}