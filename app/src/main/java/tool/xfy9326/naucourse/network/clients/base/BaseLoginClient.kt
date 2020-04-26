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

// 基础网络客户端
abstract class BaseLoginClient(private var loginInfo: LoginInfo) : BaseNetworkClient() {
    private val loginLock = ReentrantLock()

    fun setLoginInfo(loginInfo: LoginInfo) {
        this.loginInfo = loginInfo
    }

    fun getLoginInfo() = this.loginInfo

    // 获取登录前对页面的一次请求（获取登录参数）
    abstract fun getBeforeLoginResponse(): Response

    // 登录（会主动请求一次页面获取登录参数）
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

    // 登录
    protected abstract fun login(beforeLoginResponse: Response): LoginResponse

    // 注销 内部使用
    protected abstract fun logoutInternal(): Boolean

    // 注销
    fun logout(): Boolean =
        try {
            logoutInternal()
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<BaseLoginClient>(e)
            false
        }

    // 通过返回的数据判断是否登录成功
    abstract fun validateLoginWithResponse(responseContent: String, responseUrl: HttpUrl): Boolean

    // 使用可自动登录的方式进行请求
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
            // 加锁防止重复登录
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

                // 等待登录完成后重试
                try {
                    loginLock.lock()
                } finally {
                    loginLock.unlock()
                }

                newAutoLoginCall(request)
            }
        }
    }

    // 判断是否可以用返回的数据去登录
    abstract fun validateUseResponseToLogin(url: HttpUrl, content: String): Boolean

    // 判断是否不在登录页面
    protected open fun validateNotInLoginPage(responseContent: String): Boolean = true

    fun newAutoLoginCall(url: HttpUrl): Response = newAutoLoginCall(Request.Builder().url(url).build())
}