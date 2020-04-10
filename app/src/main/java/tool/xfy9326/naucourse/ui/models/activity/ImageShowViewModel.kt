package tool.xfy9326.naucourse.ui.models.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrl
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.network.SimpleNetworkManager
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.PathUtils

class ImageShowViewModel : BaseViewModel() {
    private val imageOperationMutex = Mutex()

    val image = EventLiveData<Bitmap?>()
    val imageShareUri = EventLiveData<Uri>()
    val imageOperation = EventLiveData<ImageOperationType>()
    val imageDownloadFailed = NotifyLivaData()

    fun loadBitmap(url: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                SimpleNetworkManager.getClient().newClientCall(url.toHttpUrl()).use {
                    if (it.isSuccessful) {
                        if (it.body?.byteStream() != null) {
                            it.body?.byteStream()?.let { input ->
                                image.postEventValue(BitmapFactory.decodeStream(input))
                            }
                        } else {
                            imageDownloadFailed.notifyEvent()
                        }
                    } else {
                        imageDownloadFailed.notifyEvent()
                    }
                }
            } catch (e: Exception) {
                ExceptionUtils.printStackTrace<ImageShowViewModel>(e)
                imageDownloadFailed.notifyEvent()
            }
        }
    }

    fun saveNewsImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                val uri = ImageUtils.saveImage(
                    PathUtils.getUrlFileName(source),
                    bitmap,
                    recycle = false,
                    saveToLocal = false,
                    dirName = Constants.Image.DIR_NEWS_DETAIL_IMAGE
                )
                if (uri == null) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_FAILED)
                } else {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_SUCCESS)
                }
            }
        }
    }

    fun shareImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                val uri = ImageUtils.saveImage(
                    PathUtils.getUrlFileName(source),
                    bitmap,
                    recycle = false,
                    dirName = Constants.Image.DIR_NEWS_DETAIL_IMAGE,
                    fileProviderUri = true
                )
                if (uri == null) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SHARE_FAILED)
                } else {
                    imageShareUri.postEventValue(uri)
                }
            }
        }
    }
}