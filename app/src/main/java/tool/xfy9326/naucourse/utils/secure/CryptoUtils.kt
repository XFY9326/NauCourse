package tool.xfy9326.naucourse.utils.secure

import tool.xfy9326.naucourse.io.db.UUIDDBHelper
import java.util.*

object CryptoUtils {
    init {
        System.loadLibrary("Secure")
    }

    @Synchronized
    private fun readUUID(): String = synchronized(this) {
        val uuid = UUIDDBHelper.readUUID()
        if (uuid == null) {
            val newUUID = UUID.randomUUID().toString()
            UUIDDBHelper.saveUUID(newUUID)
            return newUUID
        } else {
            uuid
        }
    }

    fun encryptText(content: String) = encryptText(content, readUUID())

    fun decryptText(content: String) = decryptText(content, readUUID())

    private external fun encryptText(content: String, key: String): String

    private external fun decryptText(content: String, key: String): String
}