package tool.xfy9326.naucourse.utils.io

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.ImageUriUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object ImageIOUtils {
    private const val FILE_MODE_READ = "r"

    fun readBitmap(file: File): Bitmap? {
        try {
            FileInputStream(file).use {
                return BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<ImageIOUtils>(e)
        }
        return null
    }

    fun modifyLocalImage(
        file: File, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.WEBP, quality: Int = 100
    ): Boolean {
        try {
            if (file.exists()) {
                val bitmap = FileInputStream(file).use {
                    BitmapFactory.decodeStream(it)
                }
                file.delete()
                val result = FileOutputStream(file).use {
                    bitmap.compress(compressFormat, quality, it).apply {
                        it.flush()
                    }
                }
                bitmap.recycle()
                return result
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<ImageUriUtils>(e)
        }
        return false
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
            ExceptionUtils.printStackTrace<ImageIOUtils>(e)
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
                    val result = bitmap.compress(compressFormat, quality, it)
                    it.flush()

                    if (recycle) {
                        bitmap.recycle()
                    }
                    return result
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<ImageIOUtils>(e)
        }
        return false
    }

    fun saveImageFromUri(uri: Uri, file: File, overWrite: Boolean = true): Boolean {
        try {
            if (file.exists() || file.mkdirs()) {
                if (file.exists()) {
                    if (overWrite) {
                        if (!file.delete()) throw IOException("File Delete Failed!")
                    } else {
                        return false
                    }
                }
                App.instance.contentResolver.openAssetFileDescriptor(uri, FILE_MODE_READ)?.createInputStream()?.channel?.use { input ->
                    FileOutputStream(file).channel.use { output ->
                        var size: Long = input.size()
                        while (size > 0) {
                            val count: Long = output.transferFrom(input, 0, input.size())
                            if (count > 0) {
                                size -= count
                            }
                        }

                        return true
                    }
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<ImageIOUtils>(e)
        }
        return false
    }
}