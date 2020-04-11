package tool.xfy9326.naucourse.ui.models.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.info.methods.NewsInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.PathUtils
import java.util.concurrent.locks.ReentrantLock

class NewsDetailViewModel : BaseViewModel() {
    private val imageOperationMutex = Mutex()
    private val loadingLock = ReentrantLock()

    val newsDetail = MutableLiveData<GeneralNewsDetail>()
    val isRefreshing = EventLiveData(false)
    val errorNotifyType = EventLiveData<ContentErrorReason>()
    val imageOperation = EventLiveData<ImageOperationType>()
    val imageShareUri = EventLiveData<Uri>()

    fun requestNewsDetail(url: HttpUrl, postSource: GeneralNews.PostSource) {
        if (loadingLock.tryLock()) {
            viewModelScope.launch {
                try {
                    isRefreshing.postEventValue(true)
                    withContext(Dispatchers.Default) {

                        val result = NewsInfo.getDetailNewsInfo(url, postSource)
                        if (result.isSuccess) {
                            newsDetail.postValue(result.contentData!!)
                        } else {
                            errorNotifyType.postEventValue(result.contentErrorResult)
                        }
                    }
                } finally {
                    isRefreshing.postEventValue(false)
                    loadingLock.unlock()
                }
            }
        }
    }

    fun shareNewsImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                val uri = ImageUtils.saveImage(
                    null,
                    bitmap,
                    recycle = true,
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

    fun saveNewsImage(source: String, bitmap: Bitmap) {
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