package tool.xfy9326.naucourse.utils.io

import android.content.Context
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.secure.CryptoUtils
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


object TextIOUtils {
    const val ASSETS_PATH_EULA_LICENSE = "EULA.txt"
    const val ASSETS_PATH_OPEN_SOURCE_LICENSE = "LICENSE"

    fun readAssetFileAsText(context: Context, path: String): String =
        context.assets.open(path).use {
            BufferedReader(InputStreamReader(it)).readText()
        }

    fun saveTextToFile(text: String, path: String, encrypt: Boolean = false, zipStore: Boolean = false): Boolean =
        try {
            val storeText = if (encrypt) {
                CryptoUtils.encryptText(text)
            } else {
                text
            }
            val file = File(path)
            if (BaseIOUtils.prepareFile(file)) {
                FileOutputStream(file).use {
                    val bytes = storeText.toByteArray()
                    if (zipStore) {
                        GZIPOutputStream(it).use { gzip ->
                            gzip.write(bytes)
                            gzip.flush()
                        }
                    } else {
                        it.write(bytes)
                        it.flush()
                    }
                }
            } else {
                throw IOException("File Prepared Failed!")
            }
            true
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<TextIOUtils>(e)
            false
        }

    fun readTextFromFile(path: String, encrypt: Boolean = false, zipStore: Boolean = false): String? =
        try {
            val file = File(path)
            if (file.exists()) {
                val rawText = FileInputStream(file).use {
                    val bytes = if (zipStore) {
                        GZIPInputStream(it).use { gzip ->
                            gzip.readBytes()
                        }
                    } else {
                        it.readBytes()
                    }
                    String(bytes)
                }
                if (encrypt) {
                    CryptoUtils.decryptText(rawText)
                } else {
                    rawText
                }
            } else {
                null
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<TextIOUtils>(e)
            null
        }
}