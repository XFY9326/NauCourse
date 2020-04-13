package tool.xfy9326.naucourse.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.beans.CreditCountItem
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.info.methods.MyCourseHistoryInfo
import tool.xfy9326.naucourse.providers.info.methods.MyCourseInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.courses.CreditCountUtils

class ScoreQueryViewModel : BaseViewModel() {
    @Volatile
    private var hasInit = false
    private val initLock = Mutex()

    private val dataGetMutex = Mutex()

    private var courseHistoryTemp: List<CourseHistory> = emptyList()
    private var courseScoreTemp: List<CourseScore> = emptyList()

    val isRefreshing = MutableLiveData(false)
    val errorMsg = EventLiveData<ContentErrorReason>()
    val courseHistory = MutableLiveData<List<CourseHistory>>()
    val courseScore = MutableLiveData<List<CourseScore>>()
    val scrollToTop = NotifyLivaData()
    val credit = EventLiveData<Float>()
    val creditCountStatus = EventLiveData<CreditCountStatus>()
    val creditCourseSelect = EventLiveData<Pair<ArrayList<CreditCountItem>, ArrayList<CreditCountItem>>>()

    enum class CreditCountStatus {
        EMPTY_DATA,
        DATA_LOADING
    }

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
                courseHistoryTemp = courseHistoryData.data!!.asList()
                courseHistory.postValue(courseHistoryTemp)
            } else {
                errorMsg.postEventValue(courseHistoryData.errorReason)
            }

            val courseScoreData = courseScoreAsync.await()
            if (courseScoreData.isSuccess) {
                courseScoreTemp = courseScoreData.data!!.asList()
                courseScore.postValue(courseScoreTemp)
            } else {
                errorMsg.postEventValue(courseScoreData.errorReason)
            }

            isRefreshing.postValue(false)
        }
    }

    fun requestCreditCount(currentItems: ArrayList<CreditCountItem>, historyItems: ArrayList<CreditCountItem>) {
        viewModelScope.launch(Dispatchers.Default) {
            if (historyItems.isEmpty() && currentItems.isEmpty()) {
                creditCountStatus.postEventValue(CreditCountStatus.EMPTY_DATA)
            } else {
                credit.postEventValue(CreditCountUtils.countCredit(currentItems, historyItems))
            }
        }
    }

    fun requestCreditCount() {
        viewModelScope.launch(Dispatchers.Default) {
            initLock.withLock {
                if (hasInit) {
                    val current = courseScoreTemp
                    val history = courseHistoryTemp

                    if (current.isEmpty() && history.isEmpty()) {
                        creditCountStatus.postEventValue(CreditCountStatus.EMPTY_DATA)
                    } else {
                        val currentArr = CreditCountUtils.getCountItemFromCourseScore(current)
                        val historyArr = CreditCountUtils.getCountItemFromCourseHistory(history)

                        if (historyArr.isEmpty() && currentArr.isEmpty()) {
                            creditCountStatus.postEventValue(CreditCountStatus.EMPTY_DATA)
                        } else if (currentArr.isEmpty() || currentArr.size == 1) {
                            credit.postEventValue(CreditCountUtils.countCredit(currentArr, historyArr))
                        } else {
                            creditCourseSelect.postEventValue(Pair(currentArr, historyArr))
                        }
                    }
                } else {
                    creditCountStatus.postEventValue(CreditCountStatus.DATA_LOADING)
                }
            }
        }
    }
}