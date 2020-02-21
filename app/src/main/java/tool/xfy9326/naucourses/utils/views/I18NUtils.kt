package tool.xfy9326.naucourses.utils.views

import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.network.clients.base.LoginResponse.ErrorReason
import tool.xfy9326.naucourses.ui.models.activity.LoginViewModel.LoadingProcess

object I18NUtils {
    fun getErrorMsgResId(errorReason: ErrorReason): Int? =
        when (errorReason) {
            ErrorReason.ALREADY_LOGIN -> R.string.already_login
            ErrorReason.INPUT_ERROR -> R.string.input_error
            ErrorReason.PASSWORD_ERROR -> R.string.password_error
            ErrorReason.SERVER_ERROR -> R.string.server_error
            ErrorReason.UNKNOWN -> R.string.unknown_error
            ErrorReason.NONE -> null
        }

    fun getLoadingProcessResId(loadingProcess: LoadingProcess): Int? =
        when (loadingProcess) {
            LoadingProcess.LOGGING_SSO -> R.string.is_logging
            LoadingProcess.LOGGING_JWC -> R.string.is_logging_jwc
            LoadingProcess.CACHING -> R.string.is_caching
            LoadingProcess.NONE -> null
        }
}