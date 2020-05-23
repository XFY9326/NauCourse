package tool.xfy9326.naucourse.utils.utility

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.io.ImageIOUtils
import java.io.File
import java.util.*


object ImageUtils {
    private const val TYPE_DIVIDER = "."
    private const val IMAGE_PNG = ".png"
    private const val IMAGE_JPG = ".jpg"
    private const val IMAGE_JPEG = ".jpeg"
    private const val IMAGE_WEBP = ".webp"

    private const val DIR_PICTURE_MAIN_DIR = "NauCourse"

    suspend fun saveImage(source: String, bitmap: Bitmap, imageOperationMutex: Mutex, imageOperation: EventLiveData<ImageOperationType>) =
        withContext(Dispatchers.IO) {
            imageOperationMutex.withLock {
                if (saveImageToAlbum(PathUtils.getUrlFileName(source), Constants.Image.DIR_NEWS_DETAIL_IMAGE, bitmap, false)) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_SUCCESS)
                } else {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_FAILED)
                }
            }
        }

    suspend fun shareImage(
        source: String, bitmap: Bitmap, imageOperationMutex: Mutex,
        imageOperation: EventLiveData<ImageOperationType>, imageShareUri: EventLiveData<Uri>
    ) =
        withContext(Dispatchers.IO) {
            imageOperationMutex.withLock {
                val uri = createImageShareTemp(PathUtils.getUrlFileName(source), bitmap, false)
                if (uri == null) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SHARE_FAILED)
                } else {
                    imageShareUri.postEventValue(uri)
                }
            }
        }

    fun clearLocalImageBySubDir(dirName: String): Boolean {
        val path = PathUtils.getImageLocalSavePath(dirName)
        return if (path == null) {
            false
        } else {
            BaseIOUtils.deleteFile(path)
        }
    }

    fun localImageExists(fileName: String, dirName: String? = null): Boolean {
        val path = PathUtils.getImageLocalSavePath(dirName)
        return if (path == null) {
            false
        } else {
            File(path, fileName).exists()
        }
    }

    fun getLocalImageFile(fileName: String, dirName: String? = null): File? {
        val path = PathUtils.getImageLocalSavePath(dirName)
        return if (path == null) {
            null
        } else {
            File(path, fileName)
        }
    }

    @Suppress("unused")
    fun readLocalImage(fileName: String, dirName: String? = null): Bitmap? {
        val localStorageFile = getLocalImageFile(fileName, dirName)
        return if (localStorageFile?.exists() == true) {
            ImageIOUtils.readBitmap(localStorageFile)
        } else {
            null
        }
    }

    fun deleteLocalImage(fileName: String, dirName: String? = null): Boolean {
        val path = PathUtils.getImageLocalSavePath(dirName)
        return if (path == null) {
            false
        } else {
            File(path, fileName).delete()
        }
    }

    fun modifyLocalImage(
        fileName: String, dirName: String? = null,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.WEBP, quality: Int = 100
    ): Boolean {
        val path = PathUtils.getImageLocalSavePath(dirName)
        return if (path == null) {
            false
        } else {
            val localStorageFile = File(path, fileName)
            ImageIOUtils.modifyLocalImage(localStorageFile, compressFormat, quality)
        }
    }

    fun saveImageToLocalFromUri(fileName: String?, uri: Uri, dirName: String? = null, overWrite: Boolean = true): String? {
        val newFileName = fileName ?: UUID.randomUUID().toString()
        val path = PathUtils.getImageLocalSavePath(dirName)
        return if (path == null) {
            null
        } else {
            val localStorageFile = File(path, newFileName)
            if (ImageIOUtils.saveImageFromUri(uri, localStorageFile, overWrite)) newFileName else null
        }
    }

    fun saveImageToAlbum(fileName: String?, dirName: String? = null, bitmap: Bitmap, recycle: Boolean) =
        saveImage(fileName, bitmap, recycle = recycle, saveToLocal = false, dirName = dirName, addFileNameTypePrefix = true) != null

    fun createImageShareTemp(fileName: String?, bitmap: Bitmap, recycle: Boolean) =
        saveImage(
            fileName,
            bitmap,
            recycle = recycle,
            dirName = Constants.Image.DIR_SHARE_TEMP_IMAGE,
            fileProviderUri = true,
            addFileNameTypePrefix = true
        )

    // 保存图片到本地（..Android/data/{packageName}/files/Pictures/{dirName}/..）或系统相册（..Pictures/NauCourse/{dirName}/..）
    // 保存的本地的图片如果要对外分享，请使用fileProviderUri = true，并在xml里面注册路径
    private fun saveImage(
        fileName: String?,
        bitmap: Bitmap,
        recycle: Boolean = true,
        overWrite: Boolean = true,  // 仅本地存储时有效
        saveToLocal: Boolean = true, // 保存到私有目录
        fileProviderUri: Boolean = false, // 对外分享时才会用到
        dirName: String? = null,
        compressFormat: Bitmap.CompressFormat? = null, // 不添加文件名后缀时必须指定图片类型
        quality: Int = 100,
        addFileNameTypePrefix: Boolean = false // 对外分享图片时建议添加文件后缀名
    ): Uri? {
        if (!addFileNameTypePrefix && compressFormat == null) {
            error("You Must Set Compress Format If File Name Type Prefix Is Not Added!")
        }
        val newFileName = imageFileNameFix(fileName, compressFormat, addFileNameTypePrefix)
        val format = getSaveImageFormatFromFileName(newFileName, compressFormat)!!

        if (saveToLocal) {
            val path = PathUtils.getImageLocalSavePath(dirName)
            return if (path == null) {
                null
            } else {
                val localStorageFile = File(path, newFileName)
                if (ImageIOUtils.saveBitmap(
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
            }
        } else {
            val contentValues = getImageContentValues(newFileName, dirName, format, bitmap)
            val uri = App.instance.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            return if (uri != null && ImageIOUtils.saveBitmap(bitmap, uri, compressFormat = format, recycle = recycle, quality = quality)) {
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

    private fun imageFileNameFix(source: String?, compressFormat: Bitmap.CompressFormat?, addFileNameTypePrefix: Boolean): String =
        if (source == null || source.isEmpty() || source.isBlank()) {
            UUID.randomUUID().toString() + if (addFileNameTypePrefix) getFileTypePrefixByCompressType(compressFormat) else Constants.EMPTY
        } else {
            val lower = source.toLowerCase(Locale.CHINA)
            if (lower.endsWith(IMAGE_PNG) || lower.endsWith(IMAGE_JPEG) || lower.endsWith(IMAGE_JPG) || lower.endsWith(IMAGE_WEBP)) {
                if (compressFormat != null) {
                    source.substring(0, source.lastIndexOf(TYPE_DIVIDER)) +
                            if (addFileNameTypePrefix) getFileTypePrefixByCompressType(compressFormat) else Constants.EMPTY
                } else {
                    if (addFileNameTypePrefix) {
                        source
                    } else {
                        source.substring(0, source.lastIndexOf(TYPE_DIVIDER))
                    }
                }
            } else {
                source + if (addFileNameTypePrefix) getFileTypePrefixByCompressType(compressFormat) else Constants.EMPTY
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