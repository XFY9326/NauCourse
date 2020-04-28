package tool.xfy9326.naucourse.utils.secure

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.io.db.UUIDDBHelper
import java.util.*

object CryptoUtils {
    init {
        System.loadLibrary("Secure")
    }

    private val uuidMutex = Mutex()

    private suspend fun readUUID(): String = uuidMutex.withLock {
        val uuid = UUIDDBHelper.readUUID()
        if (uuid == null) {
            val newUUID = UUID.randomUUID().toString()
            UUIDDBHelper.saveUUID(newUUID)
            return newUUID
        } else {
            uuid
        }
    }

    suspend fun encryptText(content: String) = coroutineScope {
        encryptText(content, readUUID())
    }

    suspend fun decryptText(content: String) = coroutineScope {
        decryptText(content, readUUID())
    }

    private external fun encryptText(content: String, key: String): String

    private external fun decryptText(content: String, key: String): String
}