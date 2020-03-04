package tool.xfy9326.naucourses.ui.models.activity

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
import java.util.concurrent.locks.ReentrantLock

class NewsDetailViewModel : BaseViewModel() {
    private val loadingLock = ReentrantLock()

    val newsDetail = MutableLiveData<GeneralNewsDetail>()
    val isLoading = MutableLiveData<Boolean>(false)
    val errorNotifyType = EventLiveData<ContentErrorReason>()

    fun requestNewsDetail(url: HttpUrl, postSource: GeneralNews.PostSource) {
        if (loadingLock.tryLock()) {
            viewModelScope.launch {
                try {
                    isLoading.postValue(true)
                    withContext(Dispatchers.Default) {

                        val result = NewsInfo.getDetailNewsInfo(url, postSource)
                        if (result.isSuccess) {
                            newsDetail.postValue(result.contentData!!)
                        } else {
                            errorNotifyType.postEventValue(result.contentErrorResult)
                        }
                    }
                } finally {
                    isLoading.postValue(false)
                    loadingLock.unlock()
                }
            }
        }
    }
}