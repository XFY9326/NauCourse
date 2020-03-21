package tool.xfy9326.naucourses.network.clients

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.internal.closeQuietly
import okio.IOException
import org.jsoup.Jsoup
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.network.clients.base.LoginResponse
import tool.xfy9326.naucourses.network.clients.tools.NetworkTools
import tool.xfy9326.naucourses.network.clients.tools.NetworkTools.Companion.hasSameHost
import tool.xfy9326.naucourses.utils.debug.LogUtils

// http://ngx.nau.edu.cn
open class NgxClient(loginInfo: LoginInfo, private val fromUrl: HttpUrl? = null) : BaseLoginClient(loginInfo) {
    private val okHttpClient = NetworkTools.getInstance().getClient(NetworkTools.NetworkType.NGX)
    private val cookieStore = NetworkTools.getInstance().getCookieStore(NetworkTools.NetworkType.NGX)
    private val isFromPathLogin = fromUrl != null

    private val loginUrl: HttpUrl

    init {
        val loginUrlBuilder = NGX_LOGIN_URL.newBuilder()
        if (fromUrl != null) {
            loginUrlBuilder.addEncodedQueryParameter(PARAM_FROM, fromUrl.toString())
        }
        loginUrl = loginUrlBuilder.build()
    }

    companion object {
        private const val NGX_HOST = "ngx.nau.edu.cn"
        private const val PATH_WENGINE_AUTH = "wengine-auth"
        private const val PATH_LOGIN = "login"
        private const val PATH_LOGOUT = "logout"
        private const val PARAM_FROM = "from"

        private const val SELECT_POST_FORM = "form[method=POST]"
        private const val ATTR_ACTION = "action"

        private const val POST_AUTH_TYPE = "auth_type"
        private const val POST_USER_NAME = "username"
        private const val POST_PASSWORD = "password"
        private const val POST_SMS_CODE = "sms_code"

        private const val POST_AUTH_TYPE_VALUE = "local"

        private const val LOGIN_PAGE_STR = "南京审计大学应用认证"
        private const val INDEX_PAGE_STR = "首页"
        private const val CAPTCHA_HTML_STR = "<input type=\"hidden\" name=\"needCaptcha\" value=\"true\">"
        private const val PASSWORD_ERROR_STR = "用户名密码错误"

        private val NGX_LOGIN_URL =
            HttpUrl.Builder().scheme(Constants.Network.HTTP).host(NGX_HOST).addPathSegment(PATH_WENGINE_AUTH).addPathSegment(PATH_LOGIN).build()
        private val NGX_LOGOUT_URL =
            HttpUrl.Builder().scheme(Constants.Network.HTTP).host(NGX_HOST).addPathSegment(PATH_WENGINE_AUTH).addPathSegment(PATH_LOGOUT).build()

        private fun getLoginStatus(htmlContent: String): LoginResponse.ErrorReason = when {
            INDEX_PAGE_STR in htmlContent -> LoginResponse.ErrorReason.NONE
            PASSWORD_ERROR_STR in htmlContent -> LoginResponse.ErrorReason.PASSWORD_ERROR
            LOGIN_PAGE_STR in htmlContent -> LoginResponse.ErrorReason.INPUT_ERROR
            else -> LoginResponse.ErrorReason.UNKNOWN
        }

        private fun getLoginPostUrl(ssoResponseContent: String): HttpUrl {
            val action = Jsoup.parse(ssoResponseContent).selectFirst(SELECT_POST_FORM).attr(ATTR_ACTION)
            return if (action.startsWith(Constants.Network.HTTP)) {
                action.toHttpUrl()
            } else {
                val url = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(NGX_HOST).addPathSegment(PATH_WENGINE_AUTH).build().toString()
                (url + Constants.Network.DIR + action).toHttpUrl()
            }
        }

        private fun getLoginPostForm(userId: String, userPw: String): FormBody = FormBody.Builder().apply {
            add(POST_AUTH_TYPE, POST_AUTH_TYPE_VALUE)
            add(POST_SMS_CODE, Constants.EMPTY)
            add(POST_USER_NAME, userId)
            add(POST_PASSWORD, userPw)
        }.build()
    }

    override fun getNetworkClient(): OkHttpClient = okHttpClient

    override fun login(response: Response): LoginResponse {
        val responseUrl = response.request.url
        val responseContent = response.body?.string()!!
        response.closeQuietly()
        if (CAPTCHA_HTML_STR in responseContent) {
            cookieStore.clearCookies()
        }
        if (LOGIN_PAGE_STR in responseContent || responseUrl.hasSameHost(NGX_HOST)) {
            if (!isFromPathLogin && getLoginStatus(responseContent) == LoginResponse.ErrorReason.NONE) {
                return LoginResponse(true, responseUrl, responseContent)
            }
            val postForm = getLoginPostForm(getLoginInfo().userId, getLoginInfo().userPw)
            val request = response.request.newBuilder().apply {
                url(getLoginPostUrl(responseContent))
                post(postForm)
            }.build()
            newNGXCall(request).use {
                if (it.isSuccessful) {
                    val url = it.request.url
                    val content = it.body?.string()!!
                    return if (isFromPathLogin) {
                        when {
                            url.hasSameHost(fromUrl) -> {
                                LoginResponse(true, url, content)
                            }
                            url.hasSameHost(NGX_HOST) -> {
                                LoginResponse(
                                    false,
                                    loginErrorReason = getLoginStatus(content)
                                )
                            }
                            else -> {
                                LoginResponse(
                                    false,
                                    loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR
                                )
                            }
                        }
                    } else {
                        val status = getLoginStatus(content)
                        if (status == LoginResponse.ErrorReason.NONE) {
                            LoginResponse(true, url, content)
                        } else {
                            LoginResponse(
                                false,
                                loginErrorReason = getLoginStatus(content)
                            )
                        }
                    }
                } else {
                    throw java.io.IOException("NGX Login Failed!")
                }
            }
        } else if (responseUrl.hasSameHost(fromUrl)) return LoginResponse(true, responseUrl, responseContent)
        else throw IOException("NGX Jump Url Error! $responseUrl")
    }

    override fun logoutInternal(): Boolean = ngxLogout()

    fun ngxLogout(clearCookies: Boolean = true): Boolean =
        newNGXCall(Request.Builder().url(NGX_LOGOUT_URL).build()).use {
            val result = it.body?.string()!!.contains(LOGIN_PAGE_STR)
            if (clearCookies) {
                cookieStore.clearCookies()
            }
            result
        }

    override fun newClientCall(request: Request): Response = newNGXCall(request)

    private fun newNGXCall(request: Request): Response = okHttpClient.newCall(request.newBuilder().build()).execute()

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        if (isFromPathLogin) {
            responseUrl.hasSameHost(fromUrl)
        } else {
            INDEX_PAGE_STR in responseContent && LOGIN_PAGE_STR !in responseContent
        }

    override fun getBeforeLoginResponse(): Response =
        newNGXCall(Request.Builder().apply {
            url(loginUrl)
        }.build())

    final override fun newAutoLoginCall(request: Request): Response {
        val response = newClientCall(request)
        val url = response.request.url
        val content = NetworkTools.getResponseContent(response)
        return if (validateLoginWithResponse(content, url)) {
            if (validateNotInLoginPage(content)) {
                response
            } else {
                response.closeQuietly()
                newClientCall(request)
            }
        } else {
            val result = if (url.hasSameHost(NGX_HOST) || LOGIN_PAGE_STR in content) {
                login(response)
            } else {
                login()
            }
            response.closeQuietly()
            if (!result.isSuccess) {
                LogUtils.d<NgxClient>("NgxClient Auto Login Failed! Reason: ${result.loginErrorReason}")
            }
            newClientCall(request)
        }
    }
}