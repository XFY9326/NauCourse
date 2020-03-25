package tool.xfy9326.naucourse.network.clients.base

import okhttp3.HttpUrl

data class LoginResponse(
    val isSuccess: Boolean,
    val url: HttpUrl? = null,
    val htmlContent: String? = null,
    val loginErrorReason: ErrorReason = ErrorReason.NONE
) {
    enum class ErrorReason {
        NONE,
        UNKNOWN,
        PASSWORD_ERROR,
        INPUT_ERROR,
        SERVER_ERROR,
        ALREADY_LOGIN,
        CONNECTION_ERROR
    }
}