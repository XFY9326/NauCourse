package tool.xfy9326.naucourse.network.clients

import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.network.tools.NetworkTools
import tool.xfy9326.naucourse.utils.debug.LogUtils

// http://ykt.nau.edu.cn
class YktClient(loginInfo: LoginInfo) : VPNClient(loginInfo) {
    companion object {
        const val YKT_HOST = "ykt.nau.edu.cn"
        private const val URL_PATH_SSO_LOGIN = "SSlogin"
        private const val URL_LOGIN_PAGE = "PanToLogin.aspx"
        private const val URL_SSO_LOGIN_PAGE = "SSOLogin.aspx"

        private val YKT_LOGIN_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(YKT_HOST).addPathSegment(URL_PATH_SSO_LOGIN)
            .addPathSegment(URL_LOGIN_PAGE).build()

        private const val LOGIN_URL_PAGE = "login.aspx"
        private const val LOGIN_PAGE_STR = "欢迎登录"
        private const val USER_LOGIN_STR = "用户登录"
        private const val SAFE_EXIT_STR = "安全退出"

        private const val ID_FORM = "form"
        private const val ID_USERNAME = "username"
        private const val ID_TIMESTAMP = "timestamp"
        private const val ID_AUID = "auid"

        private const val ATTR_ACTION = "action"
        private const val ATTR_NAME = "name"
        private const val ATTR_VALUE = "value"
    }

    override fun validateNotInLoginPage(responseContent: String): Boolean =
        LOGIN_PAGE_STR !in responseContent && USER_LOGIN_STR !in responseContent
                && URL_SSO_LOGIN_PAGE !in responseContent && SSO_LOGIN_PAGE_STR !in responseContent

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        super.validateLoginWithResponse(
            responseContent,
            responseUrl
        ) && validateNotInLoginPage(responseContent) && LOGIN_URL_PAGE !in responseUrl.toString()

    override fun onVPNClientInLoginPage(useVPN: Boolean, newRequest: Request): Response {
        val loginResponse = newVPNCall(patchVPNRequest(useVPN, Request.Builder().url(YKT_LOGIN_URL).build()))
        if (loginResponse.isSuccessful) {
            var content = NetworkTools.getResponseContent(loginResponse)
            if (!validateLoginWithResponse(content, loginResponse.request.url)) {
                val loginResult = login(loginResponse)
                if (!loginResult.isSuccess) {
                    LogUtils.d<YktClient>("YktClient SSO Login Failed! Reason: ${loginResult.loginErrorReason} Url: ${loginResult.url}")
                } else {
                    loginResult.htmlContent?.let {
                        if (validateNotInLoginPage(it) && SAFE_EXIT_STR in it) {
                            return newVPNCall(newRequest)
                        }
                    }
                    newVPNCall(patchVPNRequest(useVPN, Request.Builder().url(YKT_LOGIN_URL).build())).use { response ->
                        response.body?.string()?.let {
                            content = it
                        }
                    }
                }
            }
            if (SAFE_EXIT_STR !in content) {
                val body = Jsoup.parse(content).body()
                val loginRequest = Request.Builder().apply {
                    url(body.getElementById(ID_FORM).attr(ATTR_ACTION))
                    post(FormBody.Builder().apply {
                        val userName = body.getElementById(ID_USERNAME)
                        val timeStamp = body.getElementById(ID_TIMESTAMP)
                        val auid = body.getElementById(ID_AUID)
                        add(userName.attr(ATTR_NAME), userName.attr(ATTR_VALUE))
                        add(timeStamp.attr(ATTR_NAME), timeStamp.attr(ATTR_VALUE))
                        add(auid.attr(ATTR_NAME), auid.attr(ATTR_VALUE))
                    }.build())
                }.build()

                newVPNCall(patchVPNRequest(useVPN, loginRequest)).closeQuietly()
            }
        } else {
            LogUtils.d<YktClient>("YktClient SSO Login Failed!")
        }
        loginResponse.closeQuietly()

        return newVPNCall(newRequest)
    }
}