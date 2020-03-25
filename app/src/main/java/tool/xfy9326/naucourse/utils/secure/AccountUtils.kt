package tool.xfy9326.naucourse.utils.secure

import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.beans.UserInfo
import tool.xfy9326.naucourse.io.dbHelpers.AppDBHelper
import tool.xfy9326.naucourse.io.dbHelpers.CoursesDBHelper
import tool.xfy9326.naucourse.io.dbHelpers.JwcDBHelper
import tool.xfy9326.naucourse.io.dbHelpers.NetworkDBHelper
import tool.xfy9326.naucourse.io.prefs.*
import tool.xfy9326.naucourse.utils.utility.IOUtils
import tool.xfy9326.naucourse.utils.utility.ImageUtils

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

    fun clearAllUserData() {
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

}