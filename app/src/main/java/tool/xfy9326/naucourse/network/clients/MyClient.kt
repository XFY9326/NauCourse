package tool.xfy9326.naucourse.network.clients

import okhttp3.HttpUrl
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.network.clients.base.LoginInfo

class MyClient(loginInfo: LoginInfo) : SSOClient(loginInfo, MY_SERVER_URL) {
    companion object {
        const val MY_SERVER = "my.nau.edu.cn"

        private val MY_SERVER_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(MY_SERVER).build()

        private const val ID_API_ERROR_STR = "设置出错"
    }

    override fun validateLoginByResponse(responseContent: String, responseUrl: HttpUrl): Boolean =
        super.validateLoginByResponse(responseContent, responseUrl) && ID_API_ERROR_STR !in responseContent
}