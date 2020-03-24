package tool.xfy9326.naucourses.ui.models.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourses.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourses.providers.info.methods.NewsInfo
import tool.xfy9326.naucourses.tools.EventLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.utility.ImageUtils
import tool.xfy9326.naucourses.utils.utility.PathUtils
import java.util.concurrent.locks.ReentrantLock

class NewsDetailViewModel : BaseViewModel() {
    private val loadingLock = ReentrantLock()

    val newsDetail = MutableLiveData<GeneralNewsDetail>()
    val isRefreshing = EventLiveData(false)
    val errorNotifyType = EventLiveData<ContentErrorReason>()
    val imageOperation = EventLiveData<ImageOperationType>()
    val imageShareUri = EventLiveData<Uri>()

    enum class ImageOperationType {
        IMAGE_SAVE_SUCCESS,
        IMAGE_SAVE_FAILED,
        IMAGE_SHARE_FAILED
    }

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

    fun saveNewsImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val uri = ImageUtils.saveImage(
                PathUtils.getUrlFileName(source),
                bitmap,
                recycle = false,
                saveToLocal = false,
                dirName = ImageUtils.DIR_NEWS_DETAIL_IMAGE
            )
            if (uri == null) {
                imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_FAILED)
            } else {
                imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_SUCCESS)
            }
        }
    }

    fun shareImage(source: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val uri = ImageUtils.saveImage(
                PathUtils.getUrlFileName(source),
                bitmap,
                recycle = false,
                dirName = ImageUtils.DIR_NEWS_DETAIL_IMAGE,
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