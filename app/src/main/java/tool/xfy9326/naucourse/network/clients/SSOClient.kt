package tool.xfy9326.naucourse.network.clients

import androidx.annotation.CallSuper
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.network.clients.base.LoginResponse
import tool.xfy9326.naucourse.network.tools.NetworkTools
import tool.xfy9326.naucourse.network.tools.NetworkTools.Companion.hasSameHost
import tool.xfy9326.naucourse.network.utils.SSOPasswordUtils
import java.io.IOException

// http://sso.nau.edu.cn
open class SSOClient(
    loginInfo: LoginInfo,
    private val serviceUrl: HttpUrl? = null,
    netWorkType: NetworkTools.NetworkType = NetworkTools.NetworkType.SSO,
    private val followLoginRedirect: Boolean = true
) :
    BaseLoginClient(loginInfo) {
    private val okHttpClient = NetworkTools.getInstance().getClient(netWorkType)
    private val noRedirectOkHttpClient = okHttpClient.newBuilder().followRedirects(false).followSslRedirects(false).build()
    private val cookieStore = NetworkTools.getInstance().getCookieStore(netWorkType)
    private val isServiceLogin = serviceUrl != null

    private val loginUrl: HttpUrl

    init {
        val loginUrlBuilder = SSO_LOGIN_URL.newBuilder()
        if (serviceUrl != null) {
            loginUrlBuilder.addQueryParameter(SSO_SERVICE_PARAM, serviceUrl.toString())
        }
        loginUrl = loginUrlBuilder.build()
    }

    companion object {
        const val SSO_HOST = "sso.nau.edu.cn"
        private const val SSO_SERVICE_PARAM = "service"
        private const val SSO_PATH = "sso"
        private const val SSO_LOGIN_PATH = "login"
        private const val SSO_LOGOUT_PATH = "logout"

        private val SSO_LOGIN_URL =
            HttpUrl.Builder().scheme(NetworkConst.HTTP).host(SSO_HOST).addPathSegment(SSO_PATH).addEncodedPathSegment(SSO_LOGIN_PATH).build()
        private val SSO_LOGOUT_URL =
            HttpUrl.Builder().scheme(NetworkConst.HTTP).host(SSO_HOST).addPathSegment(SSO_PATH).addEncodedPathSegment(SSO_LOGOUT_PATH).build()

        private const val SSO_LOGIN_PARAM_ENCRYPTED = "encrypted"
        private val SSO_LOGIN_PARAM = arrayOf("execution", SSO_LOGIN_PARAM_ENCRYPTED, "_eventId", "loginType", "submit")
        private const val SSO_LOGIN_PARAM_REMEMBER_ME = "rememberMe"
        private const val SSO_LOGIN_VALUE_TRUE = "true"

        private const val SSO_INPUT_TAG_NAME_ATTR = "name"
        private const val SSO_INPUT_TAG_VALUE_ATTR = "value"
        private const val SSO_INPUT = "#fm1 > div:nth-child(5)"
        private const val SSO_POST_FORMAT = "input[$SSO_INPUT_TAG_NAME_ATTR=%s]"
        private const val SSO_POST_USERNAME = "username"
        private const val SSO_POST_PASSWORD = "password"

        private const val SSO_LOGIN_SUCCESS_STR = "登录成功"
        private const val SSO_LOGOUT_SUCCESS_STR = "注销成功"
        private const val SSO_LOGIN_PASSWORD_ERROR_STR = "密码错误"
        private const val SSO_LOGIN_INPUT_ERROR_STR = "请勿输入非法字符"
        private const val SSO_SERVER_ERROR = "单点登录系统未正常工作"
        private const val SSO_ACCOUNT_LOCK = "账号被锁定"
        const val SSO_LOGIN_PAGE_STR = "统一身份认证登录"

        private val SSO_HEADER = Headers.headersOf(
            "Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
            "Accept-Language",
            "zh-CN,zh;q=0.9",
            "Connection",
            "keep-alive",
            "Cache-Control",
            "max-age=0",
            "Upgrade-Insecure-Requests",
            "1"
        )

        private fun getSSOLoginStatus(htmlContent: String): LoginResponse.ErrorReason = when {
            SSO_LOGIN_SUCCESS_STR in htmlContent -> LoginResponse.ErrorReason.NONE
            SSO_LOGIN_PASSWORD_ERROR_STR in htmlContent -> LoginResponse.ErrorReason.PASSWORD_ERROR
            SSO_ACCOUNT_LOCK in htmlContent -> LoginResponse.ErrorReason.ACCOUNT_LOCK_ERROR
            SSO_LOGIN_INPUT_ERROR_STR in htmlContent -> LoginResponse.ErrorReason.INPUT_ERROR
            SSO_SERVER_ERROR in htmlContent -> LoginResponse.ErrorReason.SERVER_ERROR
            else -> LoginResponse.ErrorReason.UNKNOWN
        }

        private fun getLoginPostForm(userId: String, userPw: String, ssoResponseContent: String): FormBody = FormBody.Builder().apply {
            add(SSO_LOGIN_PARAM_REMEMBER_ME, SSO_LOGIN_VALUE_TRUE)
            var encryptPassword = false
            val htmlContent = Jsoup.parse(ssoResponseContent).select(SSO_INPUT)
            for (param in SSO_LOGIN_PARAM) {
                val input = htmlContent.select(SSO_POST_FORMAT.format(param)).first()
                val value = input.attr(SSO_INPUT_TAG_VALUE_ATTR)
                add(param, value)
                if (param == SSO_LOGIN_PARAM_ENCRYPTED && value == SSO_LOGIN_VALUE_TRUE) {
                    encryptPassword = true
                }
            }
            add(SSO_POST_USERNAME, userId)
            add(SSO_POST_PASSWORD, if (encryptPassword) SSOPasswordUtils.encrypt(userPw) else userPw)
        }.build()
    }

    final override fun getNetworkClient(): OkHttpClient = okHttpClient

    @Synchronized
    final override fun login(beforeLoginResponse: Response): LoginResponse {
        return loginInternal(beforeLoginResponse, isServiceLogin)
    }

    private fun loginInternal(beforeLoginResponse: Response, isServiceLogin: Boolean): LoginResponse {
        val ssoResponseUrl = beforeLoginResponse.request.url
        val ssoResponseContent = beforeLoginResponse.body?.string()!!
        beforeLoginResponse.closeQuietly()
        if (SSO_LOGIN_PAGE_STR in ssoResponseContent || ssoResponseUrl.hasSameHost(SSO_HOST)) {
            if (!isServiceLogin && getSSOLoginStatus(ssoResponseContent) == LoginResponse.ErrorReason.NONE) {
                return LoginResponse(true, ssoResponseUrl, ssoResponseContent)
            }
            val postForm = getLoginPostForm(getLoginInfo().userId, getLoginInfo().userPw, ssoResponseContent)
            val request = beforeLoginResponse.request.newBuilder().apply {
                post(postForm)
            }.build()
            newSSOCall(request, followLoginRedirect).use {
                if (!followLoginRedirect && it.isRedirect) {
                    val url = it.headers["Location"]?.toHttpUrlOrNull()
                    return if (url == null) {
                        LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR)
                    } else {
                        onRedirectLogin(noRedirectOkHttpClient, url, it.body?.string())
                    }
                }
                if (it.isSuccessful) {
                    val url = it.request.url
                    val content = it.body?.string()!!
                    return if (isServiceLogin) {
                        when {
                            url.hasSameHost(serviceUrl) -> LoginResponse(true, url, content)
                            url.hasSameHost(SSO_HOST) -> LoginResponse(
                                false,
                                loginErrorReason = getSSOLoginStatus(content)
                            )
                            else -> LoginResponse(
                                false,
                                loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR
                            )
                        }
                    } else {
                        val status = getSSOLoginStatus(content)
                        if (status == LoginResponse.ErrorReason.NONE) {
                            LoginResponse(true, url, content)
                        } else {
                            LoginResponse(
                                false,
                                loginErrorReason = getSSOLoginStatus(content)
                            )
                        }
                    }
                } else {
                    throw IOException("SSO Login Failed!")
                }
            }
        } else if (!isServiceLogin || ssoResponseUrl.hasSameHost(serviceUrl)) return LoginResponse(true, ssoResponseUrl, ssoResponseContent)
        else throw IOException("SSO Jump Url Error! $ssoResponseUrl")
    }

    protected open fun onRedirectLogin(okHttpClient: OkHttpClient, url: HttpUrl, content: String?) =
        LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR)

    @CallSuper
    override fun logoutInternal(): Boolean = ssoLogout()

    private fun ssoLogout(clearCookies: Boolean = true): Boolean =
        newSSOCall(Request.Builder().url(SSO_LOGOUT_URL).build()).use {
            val result = it.body?.string()!!.contains(SSO_LOGOUT_SUCCESS_STR)
            if (clearCookies) {
                cookieStore.clearCookies()
            }
            result
        }

    @CallSuper
    override fun validateLoginByResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        if (isServiceLogin) {
            responseUrl.hasSameHost(serviceUrl)
        } else {
            getSSOLoginStatus(responseContent) == LoginResponse.ErrorReason.NONE
        }

    override fun newClientCall(request: Request): Response = newSSOCall(request)

    private fun newSSOCall(request: Request, followRedirect: Boolean = true): Response {
        return if (followRedirect) {
            okHttpClient.newCall(request.newBuilder().headers(SSO_HEADER).build()).execute()
        } else {
            noRedirectOkHttpClient.newCall(request.newBuilder().headers(SSO_HEADER).build()).execute()
        }
    }

    final override fun getBeforeLoginResponse(): Response =
        newSSOCall(Request.Builder().apply {
            url(loginUrl)
        }.build())

    override fun validateUseResponseToLogin(url: HttpUrl, content: String) = url.hasSameHost(SSO_HOST) || SSO_LOGIN_PAGE_STR in content
}