package tool.xfy9326.naucourse.compat

import android.annotation.SuppressLint
import tool.xfy9326.naucourse.kt.hexToByteArray
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// 来自旧版NAU课程表，主要为了适配旧版数据，不应该作为新版的加密方法使用
object AESCompat {
    private const val CIPHER_MODE = "AES"
    private const val FILL_ZERO = "0"
    private const val PASSWORD_LENGTH = 32

    // 解码旧版本的加密数据
    fun decrypt(content: String, password: String) = decrypt(content.hexToByteArray(), password)
        ?.toString(StandardCharsets.UTF_8)

    private fun createKey(password: String): SecretKeySpec {
        val sb = StringBuilder(PASSWORD_LENGTH).append(password)
        while (sb.length < PASSWORD_LENGTH) {
            sb.append(FILL_ZERO)
        }
        if (sb.length > PASSWORD_LENGTH) {
            sb.setLength(PASSWORD_LENGTH)
        }
        val bytes = sb.toString().toByteArray(StandardCharsets.UTF_8)
        return SecretKeySpec(bytes, CIPHER_MODE)
    }

    @SuppressLint("GetInstance")
    private fun decrypt(content: ByteArray, password: String): ByteArray? {
        try {
            Cipher.getInstance(CIPHER_MODE).apply {
                init(Cipher.DECRYPT_MODE, createKey(password))
                return doFinal(content)
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<AESCompat>(e)
        }
        return null
    }
}