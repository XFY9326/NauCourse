package tool.xfy9326.naucourses.utils.secure

import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.io.dbHelpers.AppDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.CoursesDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.JwcDBHelper
import tool.xfy9326.naucourses.io.dbHelpers.NetworkDBHelper
import tool.xfy9326.naucourses.io.prefs.*
import tool.xfy9326.naucourses.network.clients.base.LoginInfo
import tool.xfy9326.naucourses.utils.utility.IOUtils
import tool.xfy9326.naucourses.utils.utility.ImageUtils

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

    fun clearUserCache() {
        IOUtils.deleteFile(App.instance.cacheDir.absolutePath)
        IOUtils.deleteFile(App.instance.filesDir.absolutePath)
        IOUtils.deleteFile(App.instance.codeCacheDir.absolutePath)
        IOUtils.deleteFile(App.instance.noBackupFilesDir.absolutePath)

        ImageUtils.clearLocalImageBySubDir(ImageUtils.DIR_NEWS_DETAIL_IMAGE)

        AppDBHelper.clearAll()
        CoursesDBHelper.clearAll()
        JwcDBHelper.clearAll()
        NetworkDBHelper.clearAll()

        GsonStoreVersionPref.clear()
        InfoStoredTimePref.clear()
        AppPref.clear()
        UserPref.clear()
        SettingsPref.clear()
    }

    fun saveUserInfo(userInfo: UserInfo) {
        UserPref.UserId = userInfo.userId
        UserPref.UserPassword = userInfo.userPw
    }

    fun saveUserId(userId: String) {
        UserPref.UserId = userId
    }

    data class UserInfo(
        val userId: String,
        val userPw: String
    ) {
        fun toLoginInfo() = LoginInfo(userId, userPw)
    }
}