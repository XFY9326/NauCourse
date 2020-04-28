package tool.xfy9326.naucourse.io.prefs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.prefs.base.BasePref
import tool.xfy9326.naucourse.utils.secure.CryptoUtils

object UserPref : BasePref() {
    override val prefName: String = "User"

    private const val USER_PASSWORD = "UserPassword"

    var HasLogin by pref.boolean(defValue = false, commit = true)

    private var UserId by pref.string(commit = true)

    suspend fun readUserId() = withContext(Dispatchers.IO) {
        val userId = UserId
        return@withContext if (userId == null) {
            null
        } else {
            CryptoUtils.decryptText(userId)
        }
    }

    suspend fun saveUserId(userId: String) = withContext(Dispatchers.IO) {
        UserId = CryptoUtils.encryptText(userId)
    }

    private var UserPassword by pref.string(USER_PASSWORD, commit = true)

    suspend fun readUserPassword() = withContext(Dispatchers.IO) {
        val userPassword = UserPassword
        return@withContext if (userPassword == null) {
            null
        } else {
            CryptoUtils.decryptText(userPassword)
        }
    }

    suspend fun saveUserPassword(userPassword: String) = withContext(Dispatchers.IO) {
        UserPassword = CryptoUtils.encryptText(userPassword)
    }
}