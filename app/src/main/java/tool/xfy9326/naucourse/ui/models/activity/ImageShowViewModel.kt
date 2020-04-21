package tool.xfy9326.naucourse.ui.models.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.PathUtils

class ImageShowViewModel : BaseViewModel() {
    private val imageOperationMutex = Mutex()

    val imageShareUri = EventLiveData<Uri>()
    val imageOperation = EventLiveData<ImageOperationType>()

    fun saveImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                if (ImageUtils.saveImageToAlbum(PathUtils.getUrlFileName(source), Constants.Image.DIR_NEWS_DETAIL_IMAGE, bitmap, false)) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_SUCCESS)
                } else {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_FAILED)
                }
            }
        }
    }

    fun shareImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                val uri = ImageUtils.createImageShareTemp(PathUtils.getUrlFileName(source), bitmap, false)
                if (uri == null) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SHARE_FAILED)
                } else {
                    imageShareUri.postEventValue(uri)
                }
            }
        }
    }
}