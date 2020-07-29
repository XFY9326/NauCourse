package tool.xfy9326.naucourse.network.clients

import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.network.clients.base.LoginResponse

// http://alstu.nau.edu.cn
class AlstuClient(loginInfo: LoginInfo) : VPNClient(loginInfo) {
    companion object {
        const val ALSTU_HOST = "alstu.nau.edu.cn"
        private const val ALSTU_DEFAULT_ASPX = "default.aspx"
        private const val ALSTU_PAGE_STR = "奥蓝学生管理信息系统"
        private const val ALSTU_ERROR_LOGIN_STR = "location=\"LOGIN.ASPX\";"
        private const val SSO_LOGIN_NO_SERVICE_SUCCESS_STR = "您已经成功登录统一身份认证系统"

        val ALSTU_INDEX_URL =
            HttpUrl.Builder().scheme(NetworkConst.HTTP).host(ALSTU_HOST).addPathSegment(ALSTU_DEFAULT_ASPX).build()

        private fun isAlstuIndexUrl(url: HttpUrl) = url.pathSegments.last().equals(ALSTU_DEFAULT_ASPX, true)
    }

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        super.validateLoginWithResponse(responseContent, responseUrl) && SSO_LOGIN_NO_SERVICE_SUCCESS_STR !in responseContent &&
                ALSTU_ERROR_LOGIN_STR !in responseContent && ALSTU_PAGE_STR in responseContent

    override fun login(): LoginResponse {
        val response = super.login()
        if (response.isSuccess && SSO_LOGIN_NO_SERVICE_SUCCESS_STR in response.htmlContent!!) {
            return LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR)
        }
        return response
    }

    fun newAlstuNoIndexCall(request: Request): Response {
        val response = newAutoLoginCall(request)
        return if (isAlstuIndexUrl(response.request.url)) {
            newAutoLoginCall(request)
        } else {
            response
        }
    }
}