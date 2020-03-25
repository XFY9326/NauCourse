package tool.xfy9326.naucourse.ui.models.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.info.methods.NewsInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.debug.LogUtils

class NewsViewModel : BaseViewModel() {
    @Volatile
    private var lastNewsHash: Int = 0

    val newsList = MutableLiveData<List<GeneralNews>>()
    val isRefreshing = MutableLiveData<Boolean>()
    val errorMsg = EventLiveData<ContentErrorReason>()

    override fun onInitView(isRestored: Boolean) {
        if (!isRestored) {
            isRefreshing.postValue(true)
            viewModelScope.launch(Dispatchers.Default) {
                val newsInfoResult = NewsInfo.getInfo(loadCache = true)
                if (newsInfoResult.isSuccess) {
                    lastNewsHash = newsInfoResult.data!!.hashCode()
                    newsList.postValue(newsInfoResult.data)
                } else {
                    LogUtils.d<NewsViewModel>("News Info Init Error: ${newsInfoResult.errorReason}")
                }
                isRefreshing.postValue(false)
            }
            refreshNewsList()
        }
    }

    @Synchronized
    fun refreshNewsList() {
        isRefreshing.postValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val newsInfoResult = NewsInfo.getInfo(AppPref.readShowNewsType())
            if (newsInfoResult.isSuccess) {
                val newsHashCode = newsInfoResult.data!!.hashCode()
                if (lastNewsHash != newsHashCode) {
                    lastNewsHash = newsHashCode
                    newsList.postValue(newsInfoResult.data)
                    if (newsInfoResult.data.isEmpty()) {
                        errorMsg.postEventValue(ContentErrorReason.EMPTY_DATA)
                    }
                } else {
                    LogUtils.i<NewsViewModel>("New Info Don't Need Update")
                }
            } else {
                errorMsg.postEventValue(newsInfoResult.errorReason)
            }
            isRefreshing.postValue(false)
        }
    }
}