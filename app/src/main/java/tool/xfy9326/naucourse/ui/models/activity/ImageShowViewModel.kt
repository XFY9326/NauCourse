package tool.xfy9326.naucourse.ui.models.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.utility.ImageUtils

class ImageShowViewModel : BaseViewModel() {
    private val imageOperationMutex = Mutex()

    val imageShareUri = EventLiveData<Uri>()
    val imageOperation = EventLiveData<ImageOperationType>()

    fun saveImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch {
            ImageUtils.saveImage(source, bitmap, imageOperationMutex, imageOperation)
        }
    }

    fun shareImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch {
            ImageUtils.shareImage(source, bitmap, imageOperationMutex, imageOperation, imageShareUri)
        }
    }
}