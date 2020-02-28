package tool.xfy9326.naucourses.network

import android.content.Context
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.io.prefs.UserPref
import tool.xfy9326.naucourses.network.clients.*
import tool.xfy9326.naucourses.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.network.clients.tools.SSONetworkTools
import tool.xfy9326.naucourses.utils.secure.AccountUtils

class SSONetworkManager private constructor(context: Context, private var hasLogin: Boolean, savedLoginInfo: LoginInfo? = null) {
    private lateinit var loginInfo: LoginInfo

    private val clientMap = mapOf(
        ClientType.SSO to lazy { SSOClient(loginInfo) },
        ClientType.VPN to lazy { VPNClient(loginInfo) },
        ClientType.JWC to lazy { JwcClient(loginInfo) },
        ClientType.ALSTU to lazy { AlstuClient(loginInfo) },
        ClientType.MY to lazy { MyClient(loginInfo) }
    )

    init {
        if (savedLoginInfo != null) {
            loginInfo = savedLoginInfo
        }
        SSONetworkTools.initInstance(context.cacheDir.absolutePath)
    }

    enum class ClientType {
        SSO,
        VPN,
        JWC,
        ALSTU,
        MY
    }

    companion object {
        @Volatile
        private lateinit var instance: SSONetworkManager

        fun getInstance(): SSONetworkManager = synchronized(this) {
            if (!::instance.isInitialized) {
                instance = if (UserPref.HasLogin) {
                    SSONetworkManager(App.instance, true, AccountUtils.readUserInfo().toLoginInfo())
                } else {
                    SSONetworkManager(App.instance, false)
                }
            }
            return instance
        }
    }

    fun getClient(clientType: ClientType) = synchronized(this) {
        if (hasLogin) {
            clientMap[clientType]?.value!!
        } else {
            throw IllegalStateException("You Should Login Before Use Client! Client Type: $clientType")
        }
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
        if (!hasLogin) {
            hasLogin = true
            setLoginInfo(loginInfo)
            val result = (getClient(ClientType.SSO)).login()
            if (!result.isSuccess) {
                hasLogin = false
                clearSSOCacheAndCookies()
            }
            result
        } else {
            throw IllegalStateException("You Should Logout Before Login!")
        }
    }

    fun ssoLogout(): Boolean = synchronized(this) {
        if (hasLogin) {
            val result = (getClient(ClientType.SSO)).logout()
            hasLogin = false
            return result
        } else {
            throw IllegalStateException("You Should Login Before Logout!")
        }
    }

    fun clearSSOCacheAndCookies() {
        SSONetworkTools.cacheDir.deleteRecursively()
        SSONetworkTools.instance.getCookieStore().clearCookies()
    }
}