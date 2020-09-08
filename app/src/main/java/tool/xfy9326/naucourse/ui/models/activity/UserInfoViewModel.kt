package tool.xfy9326.naucourse.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.contents.methods.jwc.StudentIndex
import tool.xfy9326.naucourse.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel

class UserInfoViewModel : BaseViewModel() {
    val studentInfo = MutableLiveData<StudentInfo>()
    val studentPhotoUrl = EventLiveData<String>()
    val userInfoRefreshFailed = EventLiveData<ContentErrorReason>()

    override fun onInitView(isRestored: Boolean) {
        tryInit {
            updatePersonalInfo(true)
        }
        updatePersonalInfo()
    }

    fun requestStuPhoto() {
        viewModelScope.launch(Dispatchers.Default) {
            studentPhotoUrl.postEventValue(StudentIndex.JWC_STU_PHOTO_URL.toString())
        }
    }

    fun updatePersonalInfo(initLoad: Boolean = false, forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val personalInfo = PersonalInfo.getInfo(loadCache = initLoad, forceRefresh = forceRefresh)
            if (personalInfo.isSuccess) {
                studentInfo.postValue(personalInfo.data!!)
            } else if (forceRefresh) {
                userInfoRefreshFailed.postEventValue(personalInfo.errorReason)
            }
        }
    }
}