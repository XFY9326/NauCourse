package tool.xfy9326.naucourses.network.clients.base

import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.HttpStatusException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * 基础网络客户端
 * @author XFY9326
 * @param loginInfo 登录信息
 */
abstract class BaseLoginClient(private var loginInfo: LoginInfo) : BaseNetworkClient() {

    fun setLoginInfo(loginInfo: LoginInfo) {
        this.loginInfo = loginInfo
    }

    fun getLoginInfo() = this.loginInfo

    /**
     * 获取登录前对页面的一次请求（获取登录参数）
     * @return 响应
     */
    abstract fun getBeforeLoginResponse(): Response

    /**
     * 登录
     *（会主动请求一次SSO页面获取登录参数）
     * @return 登录状态
     */
    @Synchronized
    open fun login(): LoginResponse {
        return try {
            getBeforeLoginResponse().use {
                if (it.isSuccessful) {
                    login(it)
                } else {
                    LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is SocketTimeoutException, is IOException, is NullPointerException, is ConnectException ->
                    LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.CONNECTION_ERROR)
                is HttpStatusException -> LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR)
                else -> LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.UNKNOWN)
            }
        }
    }

    /**
     * 登录
     * @param ssoResponse 首次SSO请求返回的Response（用于获取登录参数）
     * @return 登录状态
     */
    abstract fun login(ssoResponse: Response): LoginResponse

    /**
     * 注销所有该客户端相关登录
     * @return 是否注销成功
     */
    abstract fun logout(): Boolean

    /**
     * 通过返回的数据判断是否登录成功
     * @param responseContent 返回的网页内容
     * @param responseUrl 跳转的URL
     * @return 是否处于登录状态
     */
    abstract fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean

    /**
     * 使用可自动登录的方式进行请求
     * @param request 请求
     * @return 响应
     */
    abstract fun newAutoLoginCall(request: Request): Response

    protected open fun validateNotInLoginPage(responseContent: String): Boolean = true

    fun newAutoLoginCall(url: HttpUrl): Response = newAutoLoginCall(Request.Builder().url(url).build())
}