package tool.xfy9326.naucourses.network

import android.content.Context
import tool.xfy9326.naucourses.network.clients.*
import tool.xfy9326.naucourses.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse

class NauNetworkManager private constructor(context: Context) {
    private lateinit var loginInfo: LoginInfo
    private var hasLogin = false

    private val clientMap = mapOf(
        ClientType.SSO to lazy { SSOClient(context, loginInfo) },
        ClientType.VPN to lazy { VPNClient(context, loginInfo) },
        ClientType.JWC to lazy { JwcClient(context, loginInfo) },
        ClientType.ALSTU to lazy { AlstuClient(context, loginInfo) },
        ClientType.MY to lazy { MyClient(context, loginInfo) }
    )

    enum class ClientType {
        SSO,
        VPN,
        JWC,
        ALSTU,
        MY
    }

    companion object {
        @Volatile
        private lateinit var instance: NauNetworkManager

        fun initInstance(context: Context) = synchronized(this) {
            if (!::instance.isInitialized) {
                instance = NauNetworkManager(context)
            }
        }

        fun getInstance(): NauNetworkManager = instance
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
            (getClient(ClientType.SSO)).login()
        } else {
            throw IllegalStateException("You Should Logout Before Login!")
        }
    }

    fun ssoLogout(): Boolean = synchronized(this) {
        if (hasLogin) {
            hasLogin = false
            (getClient(ClientType.SSO)).logout()
        } else {
            throw IllegalStateException("You Should Login Before Logout!")
        }
    }
}