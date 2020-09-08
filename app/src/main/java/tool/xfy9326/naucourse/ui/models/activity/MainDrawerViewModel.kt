package tool.xfy9326.naucourse.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.providers.info.methods.CardBalanceInfo
import tool.xfy9326.naucourse.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.update.beans.UpdateInfo
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.UpdateUtils

class MainDrawerViewModel : BaseViewModel() {
    private var hasInitFragmentShow = false
    val studentCardBalance = MutableLiveData<Float>()
    val studentInfo = MutableLiveData<StudentInfo>()
    val logoutSuccess = NotifyLivaData()
    val updateInfo = EventLiveData<UpdateInfo>()

    override fun onInitView(isRestored: Boolean) {
        tryInit(Dispatchers.Default) {
            if (AppPref.EnableAdvancedFunctions) {
                updateBalance(true)
            }
            updatePersonalInfo(true)
        }
        viewModelScope.launch(Dispatchers.Default) {
            updatePersonalInfo()
        }
    }

    @Synchronized
    fun initFragmentShow() =
        if (hasInitFragmentShow) {
            true
        } else {
            hasInitFragmentShow = true
            false
        }

    private suspend fun updatePersonalInfo(initLoad: Boolean = false) = withContext(Dispatchers.Default) {
        val personalInfo = PersonalInfo.getInfo(loadCache = initLoad)
        if (personalInfo.isSuccess) {
            studentInfo.postValue(personalInfo.data!!)
        } else {
            LogUtils.d<MainDrawerViewModel>("PersonalInfo Error: ${personalInfo.errorReason}")
        }
    }

    fun refreshPersonalInfo() {
        viewModelScope.launch(Dispatchers.Default) {
            updatePersonalInfo(true)
        }
    }

    fun updateBalance(initLoad: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val balance = CardBalanceInfo.getInfo(loadCache = initLoad)
            if (balance.isSuccess) {
                studentCardBalance.postValue(balance.data!!.mainBalance)
            } else {
                LogUtils.d<MainDrawerViewModel>("CardBalanceInfo Error: ${balance.errorReason}")
            }
        }
    }

    fun checkUpdate() {
        viewModelScope.launch(Dispatchers.Default) {
            UpdateUtils.checkUpdate(updateInfo)
        }
    }

    fun requestLogout() {
        viewModelScope.launch(Dispatchers.Default) {
            LoginNetworkManager.logout()

            LoginNetworkManager.clearAllCacheAndCookies()
            AccountUtils.clearAllUserData()
            AppWidgetUtils.clearWidget()

            logoutSuccess.notifyEvent()
        }
    }
}