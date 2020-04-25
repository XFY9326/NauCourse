package tool.xfy9326.naucourse.network.clients.base

import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.jsoup.HttpStatusException
import tool.xfy9326.naucourse.network.tools.NetworkTools
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.locks.ReentrantLock

/**
 * 基础网络客户端
 * @author XFY9326
 * @param loginInfo 登录信息
 */
abstract class BaseLoginClient(private var loginInfo: LoginInfo) : BaseNetworkClient() {
    private val loginLock = ReentrantLock()

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
     *（会主动请求一次页面获取登录参数）
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
            ExceptionUtils.printStackTrace<BaseLoginClient>(e)
            when (e) {
                is SocketTimeoutException, is IOException, is NullPointerException, is ConnectException ->
                    LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.CONNECTION_ERROR)
                is HttpStatusException, is ServerErrorException -> LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.SERVER_ERROR)
                else -> LoginResponse(false, loginErrorReason = LoginResponse.ErrorReason.UNKNOWN)
            }
        }
    }

    /**
     * 登录
     * @param beforeLoginResponse 首次SSO请求返回的Response（用于获取登录参数）
     * @return 登录状态
     */
    protected abstract fun login(beforeLoginResponse: Response): LoginResponse

    /**
     * 注销所有该客户端相关登录
     * @return 是否注销成功
     */
    protected abstract fun logoutInternal(): Boolean

    fun logout(): Boolean =
        try {
            logoutInternal()
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<BaseLoginClient>(e)
            false
        }

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
    fun newAutoLoginCall(request: Request): Response {
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
            if (loginLock.tryLock()) {
                try {
                    val result = if (validateUseResponseToLogin(url, content)) {
                        login(response)
                    } else {
                        login()
                    }
                    response.closeQuietly()
                    if (!result.isSuccess) {
                        throw IOException("Auto Login Failed! Url: $url Reason: ${result.loginErrorReason}")
                    }
                } finally {
                    loginLock.unlock()
                }
                newClientCall(request)
            } else {
                response.closeQuietly()

                loginLock.lock()
                loginLock.unlock()

                newAutoLoginCall(request)
            }
        }
    }

    abstract fun validateUseResponseToLogin(url: HttpUrl, content: String): Boolean

    protected open fun validateNotInLoginPage(responseContent: String): Boolean = true

    fun newAutoLoginCall(url: HttpUrl): Response = newAutoLoginCall(Request.Builder().url(url).build())
}