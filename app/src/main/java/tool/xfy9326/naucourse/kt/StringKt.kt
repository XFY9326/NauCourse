package tool.xfy9326.naucourse.kt

import okhttp3.internal.toHexString
import java.util.*


private const val FILL_ZERO = "0"

fun String.insert(offset: Int, str: String) = StringBuilder(this).insert(offset, str).toString()

@Suppress("unused")
fun ByteArray.toHex(): String {
    var tmp: String
    val textBuilder = StringBuilder()
    for (byte in this) {
        tmp = (byte.toInt() and 0xFF).toHexString()
        if (tmp.length == 1) {
            textBuilder.append(FILL_ZERO).append(tmp)
        } else {
            textBuilder.append(tmp)
        }
    }
    return textBuilder.toString()
}

fun String.hexToByteArray(): ByteArray {
    if (this.length < 2) {
        return ByteArray(0)
    }
    val lowerString = this.toLowerCase(Locale.CHINA)
    val result = ByteArray(lowerString.length / 2)
    for (i in result.indices) {
        val tmp = lowerString.substring(2 * i, 2 * i + 2)
        result[i] = (tmp.toInt(16) and 0xFF).toByte()
    }
    return result
}