package tool.xfy9326.naucourses.ui.models.activity

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.providers.GlobalCacheLoader
import tool.xfy9326.naucourses.tools.SingleLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.secure.AccountUtils

class LoginViewModel : BaseViewModel() {
    val isLoginLoading = SingleLiveData(false)
    val errorReasonType = SingleLiveData<LoginResponse.ErrorReason>()
    val loginProcess = SingleLiveData<LoadingProcess>()
    val loginSuccess = SingleLiveData(false)
    val cachedUserId = SingleLiveData<String>()

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
                    cachedUserId.postSingleValue(id)
                }
            }
        }
    }

    fun doLogin(userId: String, userPw: String) {
        isLoginLoading.postSingleValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val loginResult = withContext(Dispatchers.Default) {
                SSONetworkManager.clearSSOCacheAndCookies()

                loginProcess.postSingleValue(LoadingProcess.LOGGING_SSO)
                val ssoLoginResult = SSONetworkManager.ssoLogin(LoginInfo(userId, userPw))
                if (ssoLoginResult.isSuccess) {
                    loginProcess.postSingleValue(LoadingProcess.LOGGING_JWC)
                    SSONetworkManager.getClient(SSONetworkManager.ClientType.JWC).login()
                } else {
                    ssoLoginResult
                }
            }

            if (loginResult.isSuccess) {
                AccountUtils.setUserLoginStatus(true)
                AccountUtils.saveUserInfo(AccountUtils.UserInfo(userId, userPw))

                loginProcess.postSingleValue(LoadingProcess.CACHING)
                GlobalCacheLoader.loadInitCache()
                loginSuccess.postSingleValue(true)
            } else {
                errorReasonType.postSingleValue(loginResult.loginErrorReason)
                isLoginLoading.postSingleValue(false)
            }
        }
    }

    override fun onCleared() {
        loginProcess.postSingleValue(LoadingProcess.NONE)
    }
}