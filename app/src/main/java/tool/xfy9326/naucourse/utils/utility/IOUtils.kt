package tool.xfy9326.naucourse.utils.utility

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.secure.CryptoUtils
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


object IOUtils {
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
            ExceptionUtils.printStackTrace<IOUtils>(e)
            false
        }

    fun saveBitmap(
        bitmap: Bitmap, file: File, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100, recycle: Boolean = false, overWrite: Boolean = true
    ): Boolean {
        try {
            if (!bitmap.isRecycled) {
                if (file.exists() || file.mkdirs()) {
                    if (file.exists()) {
                        if (overWrite) {
                            if (!file.delete()) throw IOException("File Delete Failed!")
                        } else {
                            return false
                        }
                    }
                    FileOutputStream(file).use {
                        bitmap.compress(compressFormat, quality, it)
                        it.flush()

                        if (recycle) {
                            bitmap.recycle()
                        }
                    }
                    return true
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<IOUtils>(e)
        }
        return false
    }

    fun saveBitmap(
        bitmap: Bitmap, uri: Uri, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100, recycle: Boolean = false
    ): Boolean {
        try {
            if (!bitmap.isRecycled) {
                App.instance.contentResolver.openOutputStream(uri)?.use {
                    bitmap.compress(compressFormat, quality, it)
                    it.flush()

                    if (recycle) {
                        bitmap.recycle()
                    }
                    return true
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<IOUtils>(e)
        }
        return false
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
            ExceptionUtils.printStackTrace<IOUtils>(e)
            null
        }

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            if (file.isDirectory) {
                var result = true
                file.listFiles()?.forEach {
                    if (!deleteFile(it.absolutePath)) result = false
                }
                result
            } else {
                file.delete()
            }
        } else {
            false
        }
    }
}