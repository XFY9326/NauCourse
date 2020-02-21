package tool.xfy9326.naucourses.utils.secure

import tool.xfy9326.naucourses.io.prefs.UserPref
import java.util.*

object EncryptUtils {
    init {
        System.loadLibrary("Secure")
    }

    fun readUUID(): String = synchronized(this) {
        val uuid = UserPref.UUID
        if (uuid == null) {
            val newUUID = UUID.randomUUID().toString()
            UserPref.UUID = newUUID
            return newUUID
        } else {
            uuid
        }
    }

    external fun encryptText(content: String, key: String): String

    external fun decryptText(content: String, key: String): String
}