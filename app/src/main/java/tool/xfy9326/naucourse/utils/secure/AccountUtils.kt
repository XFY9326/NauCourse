package tool.xfy9326.naucourse.utils.secure

import kotlinx.coroutines.runBlocking
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.beans.UserInfo
import tool.xfy9326.naucourse.constants.ImageConst
import tool.xfy9326.naucourse.io.db.room.AppDB
import tool.xfy9326.naucourse.io.db.room.CoursesDB
import tool.xfy9326.naucourse.io.db.room.JwcDB
import tool.xfy9326.naucourse.io.db.room.NetworkDB
import tool.xfy9326.naucourse.io.prefs.*
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import java.util.*
import kotlin.random.Random

object AccountUtils {
    fun validateUserLoginStatus(): Boolean = UserPref.HasLogin

    fun setUserLoginStatus(isLogin: Boolean) {
        UserPref.HasLogin = isLogin
    }

    fun getSpecialNumByUserId(from: Int, to: Int) = runBlocking {
        val userId = UserPref.readUserId()?.toIntOrNull()
        val random = if (userId != null) {
            Random(userId)
        } else {
            Random
        }
        return@runBlocking random.nextInt(from, to)
    }

    @Synchronized
    fun getAccountOpenId() = runBlocking {
        if (UserPref.OpenId == null) {
            val userId = UserPref.readUserId()
            UserPref.OpenId =
                if (userId != null) {
                    UUID.nameUUIDFromBytes(userId.toByteArray()).toString()
                } else {
                    UUID.randomUUID().toString()
                }
        }
        return@runBlocking UserPref.OpenId!!
    }

    suspend fun readSavedCacheUserId(): String? = UserPref.readUserId()

    suspend fun readUserInfo(): UserInfo {
        val hasLogin = UserPref.HasLogin
        val userId = UserPref.readUserId()
        val userPw = UserPref.readUserPassword()
        return if (hasLogin && userId != null && userPw != null) {
            UserInfo(userId, userPw)
        } else {
            error("You Can't Read User Info While Not Login or Not Completely Saved!")
        }
    }

    fun clearAllUserData() {
        BaseIOUtils.deleteFile(App.instance.cacheDir.absolutePath)
        BaseIOUtils.deleteFile(App.instance.filesDir.absolutePath)
        BaseIOUtils.deleteFile(App.instance.codeCacheDir.absolutePath)

        ImageUtils.clearLocalImageBySubDir(ImageConst.DIR_NEWS_DETAIL_IMAGE)
        ImageUtils.clearLocalImageBySubDir(ImageConst.DIR_APP_IMAGE)

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

    suspend fun saveUserInfo(userInfo: UserInfo) {
        UserPref.saveUserId(userInfo.userId)
        UserPref.saveUserPassword(userInfo.userPw)
    }

    suspend fun saveUserId(userId: String) {
        UserPref.saveUserId(userId)
    }

}