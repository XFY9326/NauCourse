package tool.xfy9326.naucourse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.db.NetworkDBHelper
import tool.xfy9326.naucourse.io.prefs.UserPref
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.network.clients.MyClient
import tool.xfy9326.naucourse.network.clients.NGXClient
import tool.xfy9326.naucourse.network.clients.SSOClient
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.network.clients.base.LoginResponse
import tool.xfy9326.naucourse.network.tools.NetworkTools
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.secure.AccountUtils

// 可登录客户端管理
object LoginNetworkManager {
    private lateinit var loginInfo: LoginInfo
    private val loginMutex = Mutex()

    suspend fun initLoginInfo() = withContext(Dispatchers.Default) {
        if (UserPref.HasLogin) {
            loginInfo = AccountUtils.readUserInfo().toLoginInfo()
        }
    }

    private val clientMap = mapOf(
        ClientType.SSO to lazy { SSOClient(loginInfo) },
        ClientType.JWC to lazy { JwcClient(loginInfo) },
        ClientType.MY to lazy { MyClient(loginInfo) },
        ClientType.NGX to lazy { NGXClient(loginInfo) }
    )

    enum class ClientType {
        SSO,
        JWC,
        MY,
        NGX
    }

    fun getClient(clientType: ClientType) = synchronized(this) {
        return@synchronized if (::loginInfo.isInitialized) {
            clientMap[clientType]?.value!!
        } else {
            error("Login Info Must Init First")
        }
    }

    private fun setLoginInfo(loginInfo: LoginInfo) {
        this.loginInfo = loginInfo
        clientMap.forEach {
            if (it.value.isInitialized()) {
                it.value.value.setLoginInfo(loginInfo)
            }
        }
    }

    suspend fun login(loginInfo: LoginInfo): LoginResponse = loginMutex.withLock {
        setLoginInfo(loginInfo)
        val ssoResult = getClient(ClientType.SSO).login()
        if (!ssoResult.isSuccess) {
            NetworkTools.getInstance().getCookieStore(NetworkTools.NetworkType.SSO).clearCookies()
        }

        LogUtils.d<LoginNetworkManager>("Login Result:  SSO: ${ssoResult.isSuccess}")

        ssoResult
    }

    suspend fun logout() = loginMutex.withLock {
        val jwcLogout = (getClient(ClientType.JWC)).logout()
        val ngxLogout = (getClient(ClientType.NGX)).logout()
        val ssoLogout = (getClient(ClientType.SSO)).logout()
        LogUtils.d<LoginNetworkManager>("Logout Result: Jwc: $jwcLogout  NGX: $ngxLogout  SSO: $ssoLogout")
    }

    fun clearAllCacheAndCookies() {
        NetworkTools.cacheDir.deleteRecursively()
        NetworkDBHelper.clearAll()
    }
}