package tool.xfy9326.naucourses.utils.secure

import tool.xfy9326.naucourses.io.prefs.UserPref
import tool.xfy9326.naucourses.network.clients.base.LoginInfo

object AccountUtils {
    fun validateUserLoginStatus(): Boolean = UserPref.HasLogin

    fun setUserLoginStatus(isLogin: Boolean) {
        UserPref.HasLogin = isLogin
    }

    fun readSavedCacheUserId(): String? = UserPref.UserId

    fun readUserInfo(): UserInfo {
        val hasLogin = UserPref.HasLogin
        val userId = UserPref.UserId
        val userPw = UserPref.UserPassword
        return if (hasLogin && userId != null && userPw != null) {
            UserInfo(userId, userPw)
        } else {
            throw IllegalStateException("You Can't Read User Info While Not Login or Not Completely Saved!")
        }
    }

    fun clearUserPw() = UserPref.remove(UserPref.USER_PASSWORD)

    fun saveUserInfo(userInfo: UserInfo) {
        UserPref.UserId = userInfo.userId
        UserPref.UserPassword = userInfo.userPw
    }

    data class UserInfo(
        val userId: String,
        val userPw: String
    ) {
        fun toLoginInfo() = LoginInfo(userId, userPw)
    }
}