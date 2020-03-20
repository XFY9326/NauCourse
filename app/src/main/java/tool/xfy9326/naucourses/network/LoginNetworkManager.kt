package tool.xfy9326.naucourses.network

import tool.xfy9326.naucourses.io.dbHelpers.NetworkDBHelper
import tool.xfy9326.naucourses.io.prefs.UserPref
import tool.xfy9326.naucourses.network.clients.*
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.network.clients.tools.NetworkTools
import tool.xfy9326.naucourses.utils.secure.AccountUtils
import tool.xfy9326.naucourses.utils.utility.LogUtils

object LoginNetworkManager {
    private lateinit var loginInfo: LoginInfo

    init {
        if (UserPref.HasLogin) {
            loginInfo = AccountUtils.readUserInfo().toLoginInfo()
        }
    }

    private val clientMap = mapOf(
        ClientType.SSO to lazy { SSOClient(loginInfo) },
        ClientType.VPN to lazy { VPNClient(loginInfo) },
        ClientType.JWC to lazy { JwcClient(loginInfo) },
        ClientType.ALSTU to lazy { AlstuClient(loginInfo) },
        ClientType.YKT to lazy { YktClient(loginInfo) },
        ClientType.NGX to lazy { NgxClient(loginInfo) }
    )

    enum class ClientType {
        SSO,
        VPN,
        JWC,
        ALSTU,
        YKT,
        NGX
    }

    fun getClient(clientType: ClientType) = synchronized(this) {
        clientMap[clientType]?.value!!
    }

    private fun setLoginInfo(loginInfo: LoginInfo) {
        this.loginInfo = loginInfo
        clientMap.forEach {
            if (it.value.isInitialized()) {
                it.value.value.setLoginInfo(loginInfo)
            }
        }
    }

    fun login(loginInfo: LoginInfo): LoginResponse = synchronized(this) {
        setLoginInfo(loginInfo)
        val ssoResult = (getClient(ClientType.SSO)).login()
        if (!ssoResult.isSuccess) {
            NetworkTools.getInstance().getCookieStore(NetworkTools.NetworkType.SSO).clearCookies()
        }

        val vpnResult = (getClient(ClientType.VPN)).login()
        val ngxResult = (getClient(ClientType.NGX)).login()
        if (!ngxResult.isSuccess) {
            NetworkTools.getInstance().getCookieStore(NetworkTools.NetworkType.NGX).clearCookies()
        }
        LogUtils.d<LoginNetworkManager>("Login Result: VPN: ${vpnResult.isSuccess}  SSO: ${ssoResult.isSuccess}  Ngx: ${ngxResult.isSuccess}")

        ssoResult
    }

    fun logout() = synchronized(this) {
        val jwcLogout = (getClient(ClientType.JWC)).logout()
        val ssoLogout = (getClient(ClientType.SSO)).logout()
        val ngxLogout = (getClient(ClientType.NGX)).logout()
        LogUtils.d<LoginNetworkManager>("Logout Result: Jwc: $jwcLogout  SSO: $ssoLogout  Ngx: $ngxLogout")
    }

    fun clearAllCacheAndCookies() {
        NetworkTools.cacheDir.deleteRecursively()
        NetworkDBHelper.clearAll()
    }
}