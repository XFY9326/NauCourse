package tool.xfy9326.naucourses.network.clients

import okhttp3.HttpUrl
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.clients.base.LoginInfo

// http://alstu.nau.edu.cn
class AlstuClient(loginInfo: LoginInfo) : VPNClient(loginInfo) {
    companion object {
        const val ALSTU_HOST = "alstu.nau.edu.cn"
        private const val ALSTU_DEFAULT_ASPX = "default.aspx"
        private const val ALSTU_PAGE_STR = "奥蓝学生管理信息系统"
        private const val ALSTU_ERROR_LOGIN_STR = "location=\"LOGIN.ASPX\";"

        val ALSTU_INDEX_URL =
            HttpUrl.Builder().scheme(Constants.Network.HTTP).host(ALSTU_HOST).addPathSegment(ALSTU_DEFAULT_ASPX).build()
    }

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        super.validateLoginWithResponse(responseContent, responseUrl) &&
                ALSTU_ERROR_LOGIN_STR !in responseContent && ALSTU_PAGE_STR in responseContent
}