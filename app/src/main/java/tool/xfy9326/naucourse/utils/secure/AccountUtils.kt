package tool.xfy9326.naucourse.utils.secure

import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.UserInfo
import tool.xfy9326.naucourse.io.db.room.AppDB
import tool.xfy9326.naucourse.io.db.room.CoursesDB
import tool.xfy9326.naucourse.io.db.room.JwcDB
import tool.xfy9326.naucourse.io.db.room.NetworkDB
import tool.xfy9326.naucourse.io.prefs.*
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.utility.ImageUriUtils

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
        BaseIOUtils.deleteFile(App.instance.cacheDir.absolutePath)
        BaseIOUtils.deleteFile(App.instance.filesDir.absolutePath)
        BaseIOUtils.deleteFile(App.instance.codeCacheDir.absolutePath)

        ImageUriUtils.clearLocalImageBySubDir(Constants.Image.DIR_NEWS_DETAIL_IMAGE)
        ImageUriUtils.clearLocalImageBySubDir(Constants.Image.DIR_APP_IMAGE)

        AppDB.getDB().clearAll()
        CoursesDB.getDB().clearAll()
        JwcDB.getDB().clearAll()
        NetworkDB.getDB().clearAll()

        JsonStoreVersionPref.clear()
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