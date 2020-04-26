package tool.xfy9326.naucourse.network.clients.base

import okhttp3.HttpUrl

// 登录结果
data class LoginResponse(
    val isSuccess: Boolean,
    val url: HttpUrl? = null,
    val htmlContent: String? = null,
    val loginErrorReason: ErrorReason = ErrorReason.NONE
) {
    enum class ErrorReason {
        // 无错误，即成功
        NONE,

        // 未知错误
        UNKNOWN,

        // 密码错误
        PASSWORD_ERROR,

        // 输入错误
        INPUT_ERROR,

        // 服务器错误
        SERVER_ERROR,

        // 已经登录
        ALREADY_LOGIN,

        // 连接错误
        CONNECTION_ERROR
    }
}