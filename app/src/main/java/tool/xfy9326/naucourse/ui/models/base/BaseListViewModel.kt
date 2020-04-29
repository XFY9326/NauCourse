package tool.xfy9326.naucourse.ui.models.base

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.info.base.InfoResult
import tool.xfy9326.naucourse.tools.livedata.EventLiveData

abstract class BaseListViewModel<E> : BaseViewModel() {
    private val dataGetMutex = Mutex()

    val isRefreshing = MutableLiveData(false)
    val errorMsg = EventLiveData<ContentErrorReason>()
    val listData = MutableLiveData<List<E>>()

    @CallSuper
    override fun onInitView(isRestored: Boolean) {
        if (listData.value == null) {
            viewModelScope.launch(Dispatchers.Default) {
                getData(true).join()
                getData()
            }
        }
    }

    fun getData(isInit: Boolean = false, forceUpdate: Boolean = false) = viewModelScope.launch(Dispatchers.Default) {
        dataGetMutex.withLock {
            isRefreshing.postValue(true)
            val result = onUpdateData(isInit, forceUpdate)
            if (result.isSuccess) {
                listData.postValue(result.data!!)
                if (!isInit && result.data.isEmpty()) {
                    errorMsg.postEventValue(ContentErrorReason.EMPTY_DATA)
                }
            } else {
                errorMsg.postEventValue(result.errorReason)
            }
            isRefreshing.postValue(false)
        }
    }

    protected abstract suspend fun onUpdateData(isInit: Boolean, forceUpdate: Boolean): InfoResult<List<E>>
}