package tool.xfy9326.naucourses.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.io.prefs.SettingsPref
import tool.xfy9326.naucourses.network.LoginNetworkManager
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourses.providers.info.methods.CardBalanceInfo
import tool.xfy9326.naucourses.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourses.tools.NotifyLivaData
import tool.xfy9326.naucourses.ui.activities.MainDrawerActivity
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.debug.LogUtils
import tool.xfy9326.naucourses.utils.secure.AccountUtils

class MainDrawerViewModel : BaseViewModel() {
    private var hasInitFragmentShow = false
    private val fragmentTypeLock = Any()
    private lateinit var nowShowFragmentType: MainDrawerActivity.FragmentType
    val studentCardBalance = MutableLiveData<Float>()
    val studentInfo = MutableLiveData<StudentInfo>()
    val logoutSuccess = NotifyLivaData()

    override fun onInitView(isRestored: Boolean) {
        updatePersonalInfo(true)
        updateBalance(true)
        updatePersonalInfo()
    }

    @Synchronized
    fun initFragmentShow() =
        if (hasInitFragmentShow) {
            true
        } else {
            hasInitFragmentShow = true
            false
        }

    private fun updatePersonalInfo(initLoad: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val personalInfo = PersonalInfo.getInfo(loadCache = initLoad)
            if (personalInfo.isSuccess) {
                studentInfo.postValue(personalInfo.data!!)
            } else {
                LogUtils.d<MainDrawerViewModel>("PersonalInfo Error: ${personalInfo.errorReason}")
            }
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

    fun setNowShowFragment(type: MainDrawerActivity.FragmentType) {
        synchronized(fragmentTypeLock) {
            nowShowFragmentType = type
        }
    }

    fun getNowShowFragment(): MainDrawerActivity.FragmentType {
        synchronized(fragmentTypeLock) {
            if (!::nowShowFragmentType.isInitialized) {
                nowShowFragmentType = when (SettingsPref.getDefaultEnterInterface()) {
                    SettingsPref.EnterInterfaceType.COURSE_ARRANGE -> MainDrawerActivity.FragmentType.COURSE_ARRANGE
                    SettingsPref.EnterInterfaceType.COURSE_TABLE -> MainDrawerActivity.FragmentType.COURSE_TABLE
                    SettingsPref.EnterInterfaceType.NEWS -> MainDrawerActivity.FragmentType.NEWS
                }
            }
            return nowShowFragmentType
        }
    }

    fun requestLogout() {
        viewModelScope.launch(Dispatchers.Default) {
            LoginNetworkManager.logout()

            LoginNetworkManager.clearAllCacheAndCookies()
            AccountUtils.clearUserCache()

            logoutSuccess.notifyEvent()
        }
    }
}