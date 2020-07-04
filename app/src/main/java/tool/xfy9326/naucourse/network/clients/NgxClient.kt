package tool.xfy9326.naucourse.network.clients

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.internal.closeQuietly
import okio.IOException
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.network.clients.base.LoginResponse
import tool.xfy9326.naucourse.network.tools.NetworkTools
import tool.xfy9326.naucourse.network.tools.NetworkTools.Companion.hasSameHost

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
        private const val PARAM_FROM_VALUE = "gine-auth"

        private const val SELECT_POST_FORM = "form[method=POST]"
        private const val ATTR_ACTION = "action"

        private const val POST_AUTH_TYPE = "auth_type"
        private const val POST_USER_NAME = "username"
        private const val POST_PASSWORD = "password"
        private const val POST_SMS_CODE = "sms_code"

        private const val POST_AUTH_TYPE_VALUE = "local"

        private const val LOGIN_PAGE_STR = "南京审计大学应用认证"
        private const val LOGIN_STR = "登录"
        private const val INDEX_PAGE_STR = "首页"
        private const val CAPTCHA_HTML_STR = "<input type=\"hidden\" name=\"needCaptcha\" value=\"true\">"
        private const val PASSWORD_ERROR_STR = "用户名密码错误"

        private val NGX_LOGIN_URL =
            HttpUrl.Builder().scheme(Constants.Network.HTTP).host(NGX_HOST).addPathSegment(PATH_WENGINE_AUTH).addPathSegment(PATH_LOGIN).build()
        private val NGX_LOGOUT_URL =
            HttpUrl.Builder().scheme(Constants.Network.HTTP).host(NGX_HOST).addPathSegment(PATH_WENGINE_AUTH).addPathSegment(PATH_LOGOUT).build()

        private fun getLoginStatus(htmlContent: String, isFromPathLogin: Boolean): LoginResponse.ErrorReason = when {
            PASSWORD_ERROR_STR in htmlContent -> LoginResponse.ErrorReason.PASSWORD_ERROR
            LOGIN_PAGE_STR in htmlContent && LOGIN_STR in htmlContent -> LoginResponse.ErrorReason.INPUT_ERROR
            INDEX_PAGE_STR in htmlContent || isFromPathLogin -> LoginResponse.ErrorReason.NONE
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

    final override fun login(beforeLoginResponse: Response): LoginResponse {
        var responseUrl = beforeLoginResponse.request.url
        var responseContent = beforeLoginResponse.body?.string()!!
        beforeLoginResponse.closeQuietly()

        // NGX会自动跳转服务页面登录，这里重判断一次
        val fromUrl =
            if (this.isFromPathLogin) {
                this.fromUrl
            } else {
                responseUrl.queryParameter(PARAM_FROM)?.toHttpUrlOrNull()
            }
        val isFromPathLogin = this.isFromPathLogin || fromUrl != null

        // 错误次数太多需要验证码，清空cookies重试
        if (CAPTCHA_HTML_STR in responseContent) {
            cookieStore.clearCookies()
            getBeforeLoginResponse().use {
                responseUrl = beforeLoginResponse.request.url
                responseContent = beforeLoginResponse.body?.string()!!
            }
        }

        if (LOGIN_PAGE_STR in responseContent && LOGIN_STR in responseContent && responseUrl.hasSameHost(NGX_HOST)) {
            if (!isFromPathLogin && getLoginStatus(responseContent, isFromPathLogin) == LoginResponse.ErrorReason.NONE) {
                return LoginResponse(true, responseUrl, responseContent)
            }
            val postForm = getLoginPostForm(getLoginInfo().userId, getLoginInfo().userPw)
            val request = beforeLoginResponse.request.newBuilder().apply {
                url(getLoginPostUrl(responseContent))
                post(postForm)
            }.build()
            if (isFromPathLogin) {
                newNGXCall(request)
            } else {
                newNGXLoginCall(request)
            }.use {
                val url = it.request.url
                val content = it.body?.string()!!
                if (it.isSuccessful) {
                    return if (isFromPathLogin) {
                        when {
                            url.hasSameHost(fromUrl) -> LoginResponse(true, url, content)
                            url.hasSameHost(NGX_HOST) && LOGIN_PAGE_STR !in content && LOGIN_STR !in content ->
                                LoginResponse(true, url, content)
                            else -> LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR)
                        }
                    } else {
                        val status = getLoginStatus(content, isFromPathLogin)
                        if (status == LoginResponse.ErrorReason.NONE) {
                            LoginResponse(true, url, content)
                        } else {
                            LoginResponse(false, loginErrorReason = status)
                        }
                    }
                } else if (!isFromPathLogin && it.isRedirect && content.isEmpty() &&
                    url.queryParameter(PARAM_FROM)?.contains(PARAM_FROM_VALUE) == true
                ) {
                    return LoginResponse(true, url, content)
                } else {
                    throw IOException("NGX Login Failed!")
                }
            }
        } else return LoginResponse(true, responseUrl, responseContent)
    }

    override fun logoutInternal(): Boolean = ngxLogout()

    @Suppress("MemberVisibilityCanBePrivate")
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

    // Fix: java.net.ProtocolException: Too many follow-up requests: 21
    private fun newNGXLoginCall(request: Request): Response =
        okHttpClient.newBuilder().followRedirects(false).followSslRedirects(false).build()
            .newCall(request.newBuilder().build()).execute()

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean {
        val fromUrl =
            if (this.isFromPathLogin) {
                this.fromUrl
            } else {
                responseUrl
            }
        val isFromPathLogin = this.isFromPathLogin || fromUrl != null
        return if (isFromPathLogin && fromUrl != null) {
            !fromUrl.hasSameHost(NGX_HOST)
        } else {
            INDEX_PAGE_STR in responseContent && LOGIN_PAGE_STR !in responseContent
        }
    }

    override fun getBeforeLoginResponse(): Response =
        newNGXCall(Request.Builder().apply {
            url(loginUrl)
        }.build())

    override fun validateUseResponseToLogin(url: HttpUrl, content: String) =
        LOGIN_PAGE_STR in content && LOGIN_STR in content && url.hasSameHost(NGX_HOST)
}