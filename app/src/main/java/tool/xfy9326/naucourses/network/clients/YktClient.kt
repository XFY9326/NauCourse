package tool.xfy9326.naucourses.network.clients

import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.tools.SSONetworkTools

class YktClient(loginInfo: LoginInfo) : VPNClient(loginInfo) {
    companion object {
        const val YKT_HOST = "ykt.nau.edu.cn"
        private const val URL_PATH_SSO_LOGIN = "SSlogin"
        private const val URL_LOGIN_PAGE = "PanToLogin.aspx"
        private const val URL_SSO_LOGIN_PAGE = "SSOLogin.aspx"

        private val YKT_LOGIN_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(YKT_HOST).addPathSegment(URL_PATH_SSO_LOGIN)
            .addPathSegment(URL_LOGIN_PAGE).build()

        private const val LOGIN_URL_PAGE = "login.aspx"
        private const val LOGIN_PAGE_NAME_STR = "一卡通自助查询"
        private const val LOGIN_PAGE_STR = "欢迎登录"
        private const val USER_LOGIN_STR = "用户登录"

        private const val ID_FORM = "form"
        private const val ID_USERNAME = "username"
        private const val ID_TIMESTAMP = "timestamp"
        private const val ID_AUID = "auid"

        private const val ATTR_ACTION = "action"
        private const val ATTR_NAME = "name"
        private const val ATTR_VALUE = "value"
    }

    override fun validateNotInLoginPage(responseContent: String): Boolean =
        LOGIN_PAGE_NAME_STR !in responseContent && LOGIN_PAGE_STR !in responseContent && USER_LOGIN_STR !in responseContent
                && URL_SSO_LOGIN_PAGE !in responseContent

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        super.validateLoginWithResponse(responseContent, responseUrl) && LOGIN_PAGE_NAME_STR !in responseContent
                && LOGIN_PAGE_STR !in responseContent && USER_LOGIN_STR !in responseContent && URL_SSO_LOGIN_PAGE !in responseContent
                && LOGIN_URL_PAGE !in responseUrl.toString()

    override fun newClientCall(request: Request): Response {
        val useVPN = useVPN(request.url)
        val newRequest = patchVPNRequest(useVPN, request)
        val callResult = newVPNCall(newRequest)
        return if (validateNotInLoginPage(SSONetworkTools.getResponseContent(callResult))) {
            callResult
        } else {
            newVPNCall(patchVPNRequest(useVPN, Request.Builder().url(YKT_LOGIN_URL).build())).use {
                if (it.isSuccessful) {
                    val content = it.body?.string()!!
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
            }
            newVPNCall(newRequest)
        }
    }

}