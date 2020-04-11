package tool.xfy9326.naucourse.ui.models.activity

import android.graphics.Bitmap
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
            val bitmap = SimpleNetworkManager.getClient().getBitmapFromUrl(url.toHttpUrl())
            if (bitmap != null) {
                image.postEventValue(bitmap)
            } else {
                imageDownloadFailed.notifyEvent()
            }
        }
    }

    fun saveImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                val uri = ImageUtils.saveImage(
                    PathUtils.getUrlFileName(source),
                    bitmap,
                    recycle = false,
                    saveToLocal = false,
                    dirName = Constants.Image.DIR_NEWS_DETAIL_IMAGE,
                    addFileNameTypePrefix = true
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
                    dirName = Constants.Image.DIR_SHARE_TEMP_IMAGE,
                    fileProviderUri = true,
                    addFileNameTypePrefix = true
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