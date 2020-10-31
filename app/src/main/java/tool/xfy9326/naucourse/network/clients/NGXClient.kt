package tool.xfy9326.naucourse.network.clients

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.network.clients.base.LoginResponse

class NGXClient(loginInfo: LoginInfo) : SSOClient(loginInfo, NGX_CAS_LOGIN_URL, followLoginRedirect = false) {
    companion object {
        private const val NGX_HOST = "ngx.nau.edu.cn"
        private const val URL_PATH_WENGINE_AUTH = "wengine-auth"
        private const val URL_PATH_LOGIN = "login"
        private const val URL_PATH_LOGOUT = "logout"
        private const val URL_PARAM_CAS_LOGIN = "cas_login"
        private const val URL_PARAM_CAS_LOGIN_VALUE = "true"
        private const val URL_PARAM_PATH = "path"
        private const val URL_PARAM_ID = "id"
        private const val URL_PARAM_FROM = "from"
        private const val MAX_REDIRECT_AMOUNT = 10

        private val NGX_HOST_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(NGX_HOST).build()

        private val NGX_LOGIN_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(NGX_HOST).addPathSegment(URL_PATH_WENGINE_AUTH)
            .addPathSegment(URL_PATH_LOGIN).addQueryParameter(URL_PARAM_PATH, null).addQueryParameter(URL_PARAM_ID, null)
            .addQueryParameter(URL_PARAM_FROM, null).build()

        private val NGX_CAS_LOGIN_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(NGX_HOST).addPathSegment(URL_PATH_WENGINE_AUTH)
            .addPathSegment(URL_PATH_LOGIN).addQueryParameter(URL_PARAM_CAS_LOGIN, URL_PARAM_CAS_LOGIN_VALUE).build()

        private val NGX_LOGOUT_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(NGX_HOST).addPathSegment(URL_PATH_WENGINE_AUTH)
            .addPathSegment(URL_PATH_LOGOUT).build()
    }

    override fun onRedirectLogin(okHttpClient: OkHttpClient, url: HttpUrl, content: String?): LoginResponse {
        var redirectUrl = url
        var redirectCounter = 0
        while (redirectCounter < MAX_REDIRECT_AMOUNT) {
            okHttpClient.newCall(Request.Builder().url(redirectUrl).build()).execute().use {
                var redirectLocation = it.headers["Location"]
                if (redirectLocation != null && redirectLocation.startsWith(NetworkConst.DIR)) {
                    redirectLocation = "${NetworkConst.HTTP}://${NGX_HOST}${redirectLocation}"
                }
                redirectUrl = redirectLocation?.toHttpUrlOrNull()!!

                if (it.isRedirect) {
                    if (redirectUrl == NGX_LOGIN_URL || redirectUrl == NGX_HOST_URL) {
                        return LoginResponse(true, redirectUrl, it.body?.string())
                    }
                } else {
                    redirectCounter = MAX_REDIRECT_AMOUNT
                }
                redirectCounter++
            }
        }
        return super.onRedirectLogin(okHttpClient, url, content)
    }

    override fun logoutInternal() = ngxLogout() && super.logoutInternal()

    @Suppress("MemberVisibilityCanBePrivate")
    fun ngxLogout(): Boolean {
        newClientCall(NGX_LOGOUT_URL).use {
            return it.isSuccessful
        }
    }
}