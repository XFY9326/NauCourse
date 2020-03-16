package tool.xfy9326.naucourses.network

import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.io.prefs.UserPref
import tool.xfy9326.naucourses.network.clients.*
import tool.xfy9326.naucourses.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.network.clients.tools.SSONetworkTools
import tool.xfy9326.naucourses.utils.secure.AccountUtils

object SSONetworkManager {
    private lateinit var loginInfo: LoginInfo

    init {
        if (UserPref.HasLogin) {
            loginInfo = AccountUtils.readUserInfo().toLoginInfo()
        }
        SSONetworkTools.initInstance(App.instance.cacheDir.absolutePath)
    }

    private val clientMap = mapOf(
        ClientType.SSO to lazy { SSOClient(loginInfo) },
        ClientType.VPN to lazy { VPNClient(loginInfo) },
        ClientType.JWC to lazy { JwcClient(loginInfo) },
        ClientType.ALSTU to lazy { AlstuClient(loginInfo) },
        ClientType.YKT to lazy { YktClient(loginInfo) }
    )

    enum class ClientType {
        SSO,
        VPN,
        JWC,
        ALSTU,
        YKT
    }

    fun getClient(clientType: ClientType) = synchronized(this) {
        clientMap[clientType]?.value!!
    }

    private fun setLoginInfo(loginInfo: LoginInfo) {
        this.loginInfo = loginInfo
        clientMap.forEach {
            if (it.value.isInitialized()) {
                (it.value.value as BaseLoginClient).setLoginInfo(loginInfo)
            }
        }
    }

    fun ssoLogin(loginInfo: LoginInfo): LoginResponse = synchronized(this) {
        setLoginInfo(loginInfo)
        val result = (getClient(ClientType.SSO)).login()
        if (!result.isSuccess) {
            clearSSOCacheAndCookies()
        }
        result
    }

    fun ssoLogout(): Boolean = synchronized(this) {
        return (getClient(ClientType.SSO)).logout()
    }

    fun clearSSOCacheAndCookies() {
        SSONetworkTools.cacheDir.deleteRecursively()
        SSONetworkTools.instance.getCookieStore().clearCookies()
    }
}