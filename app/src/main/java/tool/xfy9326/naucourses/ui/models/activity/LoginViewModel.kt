package tool.xfy9326.naucourses.ui.models.activity

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.providers.GlobalCacheLoader
import tool.xfy9326.naucourses.tools.EventLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.secure.AccountUtils

class LoginViewModel : BaseViewModel() {
    val isLoginLoading = EventLiveData(false)
    val errorReasonType = EventLiveData<LoginResponse.ErrorReason>()
    val loginProcess = EventLiveData<LoadingProcess>()
    val loginSuccess = EventLiveData(false)
    val cachedUserId = EventLiveData<String>()

    enum class LoadingProcess {
        LOGGING_SSO,
        LOGGING_JWC,
        CACHING,
        NONE
    }

    override fun onInitView(isRestored: Boolean) {
        if (!isRestored) {
            viewModelScope.launch(Dispatchers.Default) {
                val id = AccountUtils.readSavedCacheUserId()
                if (id != null) {
                    cachedUserId.postEventValue(id)
                }
            }
        }
    }

    fun doLogin(userId: String, userPw: String) {
        isLoginLoading.postEventValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val loginResult = withContext(Dispatchers.Default) {
                SSONetworkManager.clearSSOCacheAndCookies()
                AccountUtils.saveUserId(userId)

                loginProcess.postEventValue(LoadingProcess.LOGGING_SSO)
                val ssoLoginResult = SSONetworkManager.ssoLogin(LoginInfo(userId, userPw))
                if (ssoLoginResult.isSuccess) {
                    loginProcess.postEventValue(LoadingProcess.LOGGING_JWC)
                    SSONetworkManager.getClient(SSONetworkManager.ClientType.JWC).login()
                } else {
                    ssoLoginResult
                }
            }

            if (loginResult.isSuccess) {
                AccountUtils.setUserLoginStatus(true)
                AccountUtils.saveUserInfo(AccountUtils.UserInfo(userId, userPw))

                loginProcess.postEventValue(LoadingProcess.CACHING)
                GlobalCacheLoader.loadInitCache()
                loginSuccess.postEventValue(true)
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