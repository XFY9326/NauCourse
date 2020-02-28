package tool.xfy9326.naucourses.utils.secure

import tool.xfy9326.naucourses.beans.UUIDContent
import tool.xfy9326.naucourses.providers.store.UUIDStore
import java.util.*

object CryptoUtils {
    init {
        System.loadLibrary("Secure")
        UUIDStore.initStore()
    }

    @Synchronized
    private fun readUUID(): String = synchronized(this) {
        val uuid = UUIDStore.loadStore()?.content
        if (uuid == null) {
            val newUUID = UUID.randomUUID().toString()
            UUIDStore.saveStore(UUIDContent(newUUID))
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