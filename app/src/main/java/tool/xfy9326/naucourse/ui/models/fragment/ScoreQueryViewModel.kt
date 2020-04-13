package tool.xfy9326.naucourse.ui.models.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.info.methods.MyCourseHistoryInfo
import tool.xfy9326.naucourse.providers.info.methods.MyCourseInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel

class ScoreQueryViewModel : BaseViewModel() {
    @Volatile
    private var hasInit = false
    private val initLock = Mutex()

    private val dataGetMutex = Mutex()

    val isRefreshing = MutableLiveData(false)
    val errorMsg = EventLiveData<ContentErrorReason>()
    val courseHistory = MutableLiveData<List<CourseHistory>>()
    val courseScore = MutableLiveData<List<CourseScore>>()
    val scrollToTop = NotifyLivaData()

    override fun onInitView(isRestored: Boolean) {
        if (!isRestored) {
            viewModelScope.launch(Dispatchers.Default) {
                initLock.withLock {
                    if (!hasInit) {
                        hasInit = true
                        refreshData(true).join()
                        refreshData()
                    }
                }
            }
        }
    }

    fun refreshData(isInit: Boolean = false) = viewModelScope.launch(Dispatchers.Default) {
        dataGetMutex.withLock {
            isRefreshing.postValue(true)

            val courseScoreAsync = async { MyCourseInfo.getInfo(loadCache = isInit) }
            val courseHistoryAsync = async { MyCourseHistoryInfo.getInfo(loadCache = isInit) }

            val courseHistoryData = courseHistoryAsync.await()
            if (courseHistoryData.isSuccess) {
                courseHistory.postValue(courseHistoryData.data!!.asList())
            } else {
                errorMsg.postEventValue(courseHistoryData.errorReason)
            }

            val courseScoreData = courseScoreAsync.await()
            if (courseScoreData.isSuccess) {
                courseScore.postValue(courseScoreData.data!!.asList())
            } else {
                errorMsg.postEventValue(courseScoreData.errorReason)
            }

            isRefreshing.postValue(false)
        }
    }
}