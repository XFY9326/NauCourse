package tool.xfy9326.naucourse.ui.models.activity

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.beans.UserInfo
import tool.xfy9326.naucourse.compat.OldDataCompat
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.network.clients.base.LoginResponse
import tool.xfy9326.naucourse.providers.GlobalCacheManager
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.update.beans.UpdateInfo
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import tool.xfy9326.naucourse.utils.utility.UpdateUtils

class LoginViewModel : BaseViewModel() {
    val isLoginLoading = EventLiveData(false)
    val errorReasonType = EventLiveData<LoginResponse.ErrorReason>()
    val loginProcess = EventLiveData<LoadingProcess>()
    val compatData = EventLiveData<LoginInfo>()
    val loginSuccess = NotifyLivaData()
    val updateInfo = EventLiveData<UpdateInfo>()
    val savedCacheUserId = EventLiveData<String>()

    enum class LoadingProcess {
        DATA_COMPAT,
        LOGGING_SSO,
        LOGGING_JWC,
        CACHING,
        NONE
    }

    fun checkUpdate() {
        viewModelScope.launch(Dispatchers.Default) {
            UpdateUtils.checkUpdate(updateInfo)
        }
    }

    fun requestSavedCacheUserId() {
        viewModelScope.launch(Dispatchers.Default) {
            AccountUtils.readSavedCacheUserId()?.let {
                savedCacheUserId.postEventValue(it)
            }
        }
    }

    fun doLoginFromOldData() {
        isLoginLoading.postEventValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            loginProcess.postEventValue(LoadingProcess.DATA_COMPAT)
            val loginInfo = OldDataCompat.applyCompatDataToCurrentStore()
            if (loginInfo != null) {
                compatData.postEventValue(loginInfo)
                doLogin(loginInfo.userId, loginInfo.userPw, false)
            } else {
                isLoginLoading.postEventValue(false)
            }
        }
    }

    fun changePasswordLogin(userId: String, userPw: String) {
        isLoginLoading.postEventValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val loginResult = withContext(Dispatchers.Default) {
                LoginNetworkManager.clearAllCacheAndCookies()

                loginProcess.postEventValue(LoadingProcess.LOGGING_SSO)
                val ssoLoginResult = LoginNetworkManager.login(LoginInfo(userId, userPw))
                if (ssoLoginResult.isSuccess) {
                    loginProcess.postEventValue(LoadingProcess.LOGGING_JWC)
                    LoginNetworkManager.getClient(LoginNetworkManager.ClientType.JWC).login()
                } else {
                    ssoLoginResult
                }
            }

            if (loginResult.isSuccess) {
                App.instance.mayPasswordError = false
                AccountUtils.saveUserInfo(UserInfo(userId, userPw))
                loginSuccess.notifyEvent()
            } else {
                errorReasonType.postEventValue(loginResult.loginErrorReason)
                isLoginLoading.postEventValue(false)
            }
        }
    }

    fun doLogin(userId: String, userPw: String, startLoading: Boolean = true) {
        if (startLoading) isLoginLoading.postEventValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val loginResult = withContext(Dispatchers.Default) {
                LoginNetworkManager.clearAllCacheAndCookies()
                AccountUtils.saveUserId(userId)

                loginProcess.postEventValue(LoadingProcess.LOGGING_SSO)
                val ssoLoginResult = LoginNetworkManager.login(LoginInfo(userId, userPw))
                if (ssoLoginResult.isSuccess) {
                    loginProcess.postEventValue(LoadingProcess.LOGGING_JWC)
                    LoginNetworkManager.getClient(LoginNetworkManager.ClientType.JWC).login()
                } else {
                    ssoLoginResult
                }
            }

            if (loginResult.isSuccess) {
                loginProcess.postEventValue(LoadingProcess.CACHING)
                GlobalCacheManager.loadInitCache()

                AccountUtils.setUserLoginStatus(true)
                AccountUtils.saveUserInfo(UserInfo(userId, userPw))

                loginSuccess.notifyEvent()
            } else {
                errorReasonType.postEventValue(loginResult.loginErrorReason)
                isLoginLoading.postEventValue(false)
            }
        }
    }

    override fun onCleared() {
        loginProcess.postEventValue(LoadingProcess.NONE)
    }
}