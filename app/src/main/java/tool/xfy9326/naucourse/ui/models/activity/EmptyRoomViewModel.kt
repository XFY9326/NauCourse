package tool.xfy9326.naucourse.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomInfo
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomSearchParam
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomSearchResult
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.contents.methods.jwc.EmptyRoomList
import tool.xfy9326.naucourse.providers.contents.methods.jwc.GetEmptyRoomInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel

class EmptyRoomViewModel : BaseViewModel() {
    val isLoading = EventLiveData<Boolean>()
    val searchData = MutableLiveData<EmptyRoomInfo>()
    val searchResult = MutableLiveData<Array<EmptyRoomSearchResult>>()
    val errorMsg = EventLiveData<Pair<ContentErrorReason, Boolean>>() // Boolean: 是否Toast展示错误信息并退出

    override fun onInitView(isRestored: Boolean) {
        if (tryInit()) {
            viewModelScope.launch(Dispatchers.Default) {
                isLoading.postEventValue(true)
                getSearchData(true)
            }
        }
    }

    private suspend fun getSearchData(isInit: Boolean = false) = withContext(Dispatchers.Default) {
        val result = EmptyRoomList.getContentData()
        if (result.isSuccess) {
            searchData.postValue(result.contentData!!)
        } else {
            errorMsg.postEventValue(result.contentErrorResult to isInit)
        }
        isLoading.postEventValue(false)
    }

    fun refreshSearchData() {
        viewModelScope.launch(Dispatchers.Default) {
            getSearchData()
        }
    }

    fun searchData(param: EmptyRoomSearchParam) {
        viewModelScope.launch(Dispatchers.Default) {
            isLoading.postEventValue(true)
            val result = GetEmptyRoomInfo.getContentData(param)
            if (result.isSuccess) {
                searchResult.postValue(result.contentData!!)
            } else {
                errorMsg.postEventValue(result.contentErrorResult to false)
            }
            isLoading.postEventValue(false)
        }
    }
}