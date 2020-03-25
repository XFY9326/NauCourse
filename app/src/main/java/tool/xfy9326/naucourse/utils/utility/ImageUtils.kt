package tool.xfy9326.naucourse.utils.utility

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import java.io.File
import java.util.*


object ImageUtils {
    private const val TYPE_DIVIDER = "."
    private const val IMAGE_PNG = ".png"
    private const val IMAGE_JPG = ".jpg"
    private const val IMAGE_JPEG = ".jpeg"
    private const val IMAGE_WEBP = ".webp"

    const val DIR_NEWS_DETAIL_IMAGE = "NewsDetailImage"
    private const val DIR_PICTURE_MAIN_DIR = "NauCourse"

    fun clearLocalImageBySubDir(dirName: String) = IOUtils.deleteFile(PathUtils.getImageLocalSavePath() + dirName)

    @Suppress("unused")
    fun clearAllLocalImageDir() = IOUtils.deleteFile(PathUtils.getImageLocalSavePath())

    // 保存图片到本地（..Android/data/{packageName}/files/Pictures/{dirName}/..）或系统相册（..Pictures/NauCourse/{dirName}/..）
    // 保存的本地的图片如果要对外分享，请使用fileProviderUri = true，并在xml里面注册路径
    fun saveImage(
        fileName: String?,
        bitmap: Bitmap,
        recycle: Boolean = true,
        overWrite: Boolean = true,  // 仅本地存储时有效
        saveToLocal: Boolean = true,
        fileProviderUri: Boolean = false,
        dirName: String? = null,
        compressFormat: Bitmap.CompressFormat? = null,
        quality: Int = 100
    ): Uri? {
        val newFileName = imageFileNameFix(fileName, compressFormat)
        val format = getSaveImageFormatFromFileName(newFileName, compressFormat)!!

        if (saveToLocal) {
            val localStorageFile = File(PathUtils.getImageLocalSavePath(dirName), newFileName)
            return if (IOUtils.saveBitmap(
                    bitmap,
                    localStorageFile,
                    compressFormat = format,
                    recycle = recycle,
                    overWrite = overWrite,
                    quality = quality
                )
            ) {
                if (fileProviderUri) {
                    FileProvider.getUriForFile(App.instance, Constants.FILE_PROVIDER_AUTH, localStorageFile)
                } else {
                    Uri.fromFile(localStorageFile)
                }
            } else {
                null
            }
        } else {
            val contentValues = getImageContentValues(newFileName, dirName, format, bitmap)
            val uri = App.instance.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            return if (uri != null && IOUtils.saveBitmap(bitmap, uri, compressFormat = format, recycle = recycle, quality = quality)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    @Suppress("DEPRECATION")
                    App.instance.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                }
                uri
            } else {
                null
            }
        }
    }

    private fun getImageContentValues(fileName: String, dirName: String?, format: Bitmap.CompressFormat, bitmap: Bitmap): ContentValues =
        ContentValues().apply {
            val current = System.currentTimeMillis() / 1000

            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(
                MediaStore.Images.Media.MIME_TYPE, when (format) {
                    Bitmap.CompressFormat.PNG -> Constants.MIME.IMAGE_PNG
                    Bitmap.CompressFormat.JPEG -> Constants.MIME.IMAGE_JPEG
                    Bitmap.CompressFormat.WEBP -> Constants.MIME.IMAGE_WEBP
                }
            )
            put(MediaStore.Images.Media.DATE_ADDED, current)
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            put(MediaStore.Images.ImageColumns.DATE_MODIFIED, current)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (dirName == null) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + DIR_PICTURE_MAIN_DIR)
                } else {
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator +
                                DIR_PICTURE_MAIN_DIR + File.separator + dirName
                    )
                }
                put(MediaStore.Images.ImageColumns.DATE_TAKEN, current)
            } else {
                @Suppress("DEPRECATION")
                put(
                    MediaStore.Images.Media.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + File.separator +
                            DIR_PICTURE_MAIN_DIR + File.separator + dirName + File.separator + fileName
                )
            }
        }

    private fun imageFileNameFix(source: String?, compressFormat: Bitmap.CompressFormat?): String =
        if (source == null || source.isEmpty() || source.isBlank()) {
            UUID.randomUUID().toString() + getFileTypePrefixByCompressType(compressFormat)
        } else {
            val lower = source.toLowerCase(Locale.CHINA)
            if (lower.endsWith(IMAGE_PNG) || lower.endsWith(IMAGE_JPEG) || lower.endsWith(IMAGE_JPG) || lower.endsWith(IMAGE_WEBP)) {
                if (compressFormat != null) {
                    source.substring(0, source.lastIndexOf(TYPE_DIVIDER)) + getFileTypePrefixByCompressType(compressFormat)
                } else {
                    source
                }
            } else {
                source + getFileTypePrefixByCompressType(compressFormat)
            }
        }

    private fun getSaveImageFormatFromFileName(fileName: String, compressFormat: Bitmap.CompressFormat?): Bitmap.CompressFormat? {
        val lower = fileName.toLowerCase(Locale.CHINA)
        return if (lower.endsWith(IMAGE_PNG)) {
            Bitmap.CompressFormat.PNG
        } else if (lower.endsWith(IMAGE_JPEG) || lower.endsWith(IMAGE_JPG)) {
            Bitmap.CompressFormat.JPEG
        } else if (lower.endsWith(IMAGE_WEBP)) {
            Bitmap.CompressFormat.WEBP
        } else {
            compressFormat
        }
    }

    private fun getFileTypePrefixByCompressType(compressFormat: Bitmap.CompressFormat?, defaultImagePrefix: String = IMAGE_PNG) =
        when (compressFormat) {
            Bitmap.CompressFormat.PNG -> IMAGE_PNG
            Bitmap.CompressFormat.JPEG -> IMAGE_JPEG
            Bitmap.CompressFormat.WEBP -> IMAGE_WEBP
            else -> defaultImagePrefix
        }
}