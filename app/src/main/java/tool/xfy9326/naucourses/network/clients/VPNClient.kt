package tool.xfy9326.naucourses.network.clients

import androidx.annotation.CallSuper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.network.clients.tools.NetworkTools
import tool.xfy9326.naucourses.network.clients.tools.NetworkTools.Companion.hasSameHost
import tool.xfy9326.naucourses.network.clients.tools.VPNTools
import tool.xfy9326.naucourses.utils.debug.LogUtils
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

// http://vpn.nau.edu.cn
open class VPNClient(loginInfo: LoginInfo, loginUrl: HttpUrl? = null) :
    SSOClient(
        loginInfo, if (loginUrl != null) {
            getUrlWithVPNClient(loginUrl)
        } else {
            VPN_LOGIN_URL
        }
    ) {
    protected open var forceUseVPN: Boolean = false

    companion object {
        const val VPN_HOST = "vpn.nau.edu.cn"
        private const val VPN_LOGIN_PATH = "login"
        private const val VPN_LOGOUT_PATH = "logout"

        private const val VPN_CAS_LOGIN_PARAM = "cas_login"
        private const val VPN_CAS_LOGIN_PARAM_VALUE = "true"
        private const val VPN_FROM_URL_PARAM = "fromUrl"
        private const val VPN_FROM_URL_PARAM_VALUE = "/"

        private val VPN_LOGIN_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(VPN_HOST).addPathSegment(VPN_LOGIN_PATH)
            .addQueryParameter(VPN_CAS_LOGIN_PARAM, VPN_CAS_LOGIN_PARAM_VALUE)
            .addQueryParameter(VPN_FROM_URL_PARAM, VPN_FROM_URL_PARAM_VALUE).build()
        private val VPN_LOGOUT_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(VPN_HOST).addPathSegment(VPN_LOGOUT_PATH).build()

        private const val VPN_LOGIN_PAGE_STR = "南京审计大学WEBVPN登录"
        private const val VPN_HOST_DATA_STR = "__vpn_hostname_data"
        private const val VPN_NECESSARY_STR = "通过VPN才可以访问"
        private const val ONLY_ALLOW_SCHOOL_IP_STR = "该信息仅允许校内地址访问"

        // TimeUnit.MILLISECONDS
        private const val TEST_CONNECT_TIME_OUT = 2000L

        fun isPageUsingVPN(content: String) = VPN_HOST_DATA_STR in content

        private fun getUrlWithVPNClient(url: HttpUrl): HttpUrl =
            if (!url.hasSameHost(VPN_HOST)) {
                VPNTools.buildVPNUrl(url)
            } else {
                url
            }

        fun patchVPNRequest(useVPN: Boolean, request: Request): Request {
            val vpnUrl = if (useVPN) {
                getUrlWithVPNClient(request.url)
            } else {
                request.url
            }

            val requestBuilder = request.newBuilder()
            val refererUrl = request.header(Constants.Network.HEADER_REFERER)
            if (useVPN && refererUrl != null) {
                val refererHttpUrl = refererUrl.toHttpUrl()
                if (!refererHttpUrl.hasSameHost(vpnUrl)) {
                    requestBuilder.header(Constants.Network.HEADER_REFERER, VPNTools.buildVPNUrl(refererHttpUrl).toString())
                }
            }
            requestBuilder.url(vpnUrl)
            return requestBuilder.build()
        }
    }

    @CallSuper
    override fun login(): LoginResponse {
        val ssoResult = super.login()
        return if (ssoResult.isSuccess && VPN_LOGIN_PAGE_STR !in ssoResult.htmlContent!!) {
            LoginResponse(
                false,
                loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR
            )
        } else {
            ssoResult
        }
    }

    @CallSuper
    override fun logoutInternal(): Boolean = vpnLogout() && super.logoutInternal()

    @Suppress("MemberVisibilityCanBePrivate")
    fun vpnLogout(): Boolean = newVPNCall(Request.Builder().url(VPN_LOGOUT_URL).build()).use {
        val content = it.body?.string()!!
        return it.isSuccessful && SSO_LOGIN_PAGE_STR in content
    }

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        super.validateLoginWithResponse(responseContent, responseUrl) &&
                (VPN_LOGIN_PAGE_STR !in responseContent || (SSO_LOGIN_PAGE_STR !in responseContent && VPN_HOST_DATA_STR in responseContent))

    override fun validateNotInLoginPage(responseContent: String): Boolean =
        SSO_LOGIN_PAGE_STR !in responseContent && VPN_LOGIN_PAGE_STR !in responseContent && SSO_SERVER_ERROR !in responseContent

    protected fun newVPNCall(request: Request): Response = getNetworkClient().newCall(request).execute()

    private fun validateVPNNecessary(url: HttpUrl): Boolean = try {
        getNetworkClient().newBuilder().connectTimeout(TEST_CONNECT_TIME_OUT, TimeUnit.MILLISECONDS).build()
            .newCall(Request.Builder().url(url).build()).execute().use {
                val body = it.body?.string()!!
                (it.code == Constants.Network.HTTP_FORBIDDEN_STATUS && VPN_NECESSARY_STR in body) || ONLY_ALLOW_SCHOOL_IP_STR in body
            }
    } catch (e: SocketTimeoutException) {
        true
    }

    protected fun useVPN(url: HttpUrl) = forceUseVPN || validateVPNNecessary(url)

    override fun newClientCall(request: Request): Response {
        val useVPN = useVPN(request.url)
        val newRequest = patchVPNRequest(useVPN, request)
        var callResult = newVPNCall(newRequest)
        if (!validateLoginWithResponse(NetworkTools.getResponseContent(callResult), callResult.request.url)) {
            val loginResponse = login(callResult)
            if (loginResponse.isSuccess) {
                callResult = newVPNCall(newRequest)
            } else {
                LogUtils.d<VPNClient>("VPN Login While Call Failed")
                return newVPNCall(newRequest)
            }
        }
        return if (validateNotInLoginPage(NetworkTools.getResponseContent(callResult))) {
            callResult
        } else {
            newVPNCall(newRequest)
        }
    }
}