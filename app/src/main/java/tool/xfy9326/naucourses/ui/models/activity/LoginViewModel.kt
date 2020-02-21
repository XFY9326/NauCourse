package tool.xfy9326.naucourses.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.providers.GlobalCacheLoader
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.secure.AccountUtils

class LoginViewModel : BaseViewModel() {
    val isLoginLoading = MutableLiveData<Boolean>(false)
    val errorReasonType = MutableLiveData<LoginResponse.ErrorReason>()
    val loginProcess = MutableLiveData<LoadingProcess>()
    val loginSuccess = MutableLiveData<Boolean>(false)
    val cachedUserId = MutableLiveData<String>()

    enum class LoadingProcess {
        LOGGING_SSO,
        LOGGING_JWC,
        CACHING,
        NONE
    }

    override fun onInitView() {
        viewModelScope.launch {
            val id = AccountUtils.readSavedCacheUserId()
            if (id != null) {
                cachedUserId.postValue(id)
            }
        }
    }

    fun doLogin(userId: String, userPw: String) {
        isLoginLoading.postValue(true)
        viewModelScope.launch {
            val loginResult = withContext(Dispatchers.Default) {
                SSONetworkManager.getInstance().clearSSOCacheAndCookies()

                loginProcess.postValue(LoadingProcess.LOGGING_SSO)
                val networkManager = SSONetworkManager.getInstance()
                val ssoLoginResult = networkManager.ssoLogin(LoginInfo(userId, userPw))
                if (ssoLoginResult.isSuccess) {
                    loginProcess.postValue(LoadingProcess.LOGGING_JWC)
                    networkManager.getClient(SSONetworkManager.ClientType.JWC).login()
                } else {
                    ssoLoginResult
                }
            }

            if (loginResult.isSuccess) {
                AccountUtils.setUserLoginStatus(true)
                AccountUtils.saveUserInfo(AccountUtils.UserInfo(userId, userPw))

                loginProcess.postValue(LoadingProcess.CACHING)
                GlobalCacheLoader.loadInitCache()
                loginSuccess.postValue(true)
            } else {
                errorReasonType.postValue(loginResult.loginErrorReason)
                isLoginLoading.postValue(false)
            }
        }
    }

    override fun onCleared() {
        loginProcess.postValue(LoadingProcess.NONE)
    }
}