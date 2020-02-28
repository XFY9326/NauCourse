package tool.xfy9326.naucourses.utils

import android.content.Context
import tool.xfy9326.naucourses.utils.secure.CryptoUtils
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


object IOUtils {
    const val ASSETS_PATH_EULA_LICENSE = "EULA.txt"

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
            if (file.exists() || file.mkdirs()) {
                if (file.exists() && !file.delete()) {
                    throw IOException("File Delete Failed!")
                }
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
                throw IOException("File Dirs Create Failed!")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
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
            e.printStackTrace()
            null
        }

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.delete()
    }
}