package tool.xfy9326.naucourses.network.clients

import android.content.Context
import okhttp3.HttpUrl
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.clients.base.LoginInfo

class MyClient(context: Context, loginInfo: LoginInfo) : SSOClient(context, loginInfo, MY_SSO_LOGIN_URL) {
    companion object {
        const val MY_HOST = "my1.nau.edu.cn"
        private val MY_SSO_LOGIN_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(MY_HOST).build()

        private const val API_LOGIN_ERROR_STR = "根据id查找提醒设置出错"
    }

    override fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        super.validateLoginWithResponse(responseContent, responseUrl) && API_LOGIN_ERROR_STR !in responseContent
}