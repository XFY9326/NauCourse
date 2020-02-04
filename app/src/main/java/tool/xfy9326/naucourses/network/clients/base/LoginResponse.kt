package tool.xfy9326.naucourses.network.clients.base

import okhttp3.HttpUrl

data class LoginResponse(
    val isSuccess: Boolean,
    val url: HttpUrl? = null,
    val htmlContent: String? = null,
    val loginErrorResult: ErrorResult = ErrorResult.NONE
) {
    enum class ErrorResult {
        NONE,
        UNKNOWN,
        PASSWORD_ERROR,
        INPUT_ERROR,
        SERVER_ERROR,
        ALREADY_LOGIN
    }
}