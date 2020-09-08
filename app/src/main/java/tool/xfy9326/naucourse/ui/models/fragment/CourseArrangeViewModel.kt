package tool.xfy9326.naucourse.ui.models.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.beans.*
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import tool.xfy9326.naucourse.io.store.CourseArrangeStore
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.info.methods.CourseInfo
import tool.xfy9326.naucourse.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.courses.CourseStyleUtils
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.courses.ExtraCourseUtils
import tool.xfy9326.naucourse.utils.debug.LogUtils

class CourseArrangeViewModel : BaseViewModel() {
    @Volatile
    private lateinit var todayCourseArr: Array<CourseItem>

    @Volatile
    private lateinit var tomorrowCourseArr: Array<CourseItem>

    @Volatile
    private lateinit var notThisWeekCourseArr: Array<CourseItem>

    @Volatile
    private lateinit var termDate: TermDate

    private var initJob: Job? = null

    private val arrangeRefreshMutex = Mutex()

    val nextCourseData = MutableLiveData<CourseItem?>()
    val isRefreshing = MutableLiveData<Boolean>()
    val todayCourses = MutableLiveData<Array<CourseBundle>>()
    val tomorrowCourses = MutableLiveData<Array<CourseBundle>>()
    val notThisWeekCourse = MutableLiveData<Array<CourseBundle>>()
    val notifyMsg = EventLiveData<CourseArrangeNotifyType>()
    val termDateData = MutableLiveData<TermDate?>()
    val courseDetail = EventLiveData<CourseDetail>()
    val nextCourseBundle = EventLiveData<NextCourseBundle?>()

    enum class CourseArrangeNotifyType {
        COURSE_OR_TERM_DATA_EMPTY,
        LOADING_DATA,
        NO_TODAY_COURSE
    }

    override fun onInitView(isRestored: Boolean) {
        tryInit { initJob = loadInitCache() }

        viewModelScope.launch(Dispatchers.Default) {
            if (initJob != null) {
                if (initJob?.isActive == true) initJob?.join()
                refreshArrangeCourses(updateNextCourseWidget = false)
                refreshNextCoursePosition()
            }
        }
    }

    private fun loadInitCache() = viewModelScope.launch(Dispatchers.Default) {
        val arrangeCache = CourseArrangeStore.loadStore()
        if (arrangeCache != null) {
            todayCourses.postValue(arrangeCache.todayCourseArr)
            tomorrowCourses.postValue(arrangeCache.tomorrowCourseArr)
            notThisWeekCourse.postValue(arrangeCache.notThisWeekCourseArr)

            if (arrangeCache.termDate != null) {
                termDateData.postValue(arrangeCache.termDate)
                termDate = arrangeCache.termDate
            }

            todayCourseArr = Array(arrangeCache.todayCourseArr.size) {
                arrangeCache.todayCourseArr[it].courseItem
            }
            refreshNextCoursePosition(false)

            tomorrowCourseArr = Array(arrangeCache.tomorrowCourseArr.size) {
                arrangeCache.tomorrowCourseArr[it].courseItem
            }
            notThisWeekCourseArr = Array(arrangeCache.notThisWeekCourseArr.size) {
                arrangeCache.notThisWeekCourseArr[it].courseItem
            }
        }
    }

    @Synchronized
    fun refreshArrangeCourses(showAttention: Boolean = false, updateNextCourseWidget: Boolean = true) {
        if (showAttention) {
            isRefreshing.postValue(true)
        }
        viewModelScope.launch(Dispatchers.Default) {
            arrangeRefreshMutex.withLock {
                val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
                val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }

                val termInfo = termInfoAsync.await()

                if (termInfo.isSuccess) {
                    termDate = termInfo.data!!
                    termDateData.postValue(termInfo.data)
                } else {
                    termDateData.postValue(null)
                }

                val courseInfo = courseInfoAsync.await()

                if (termInfo.isSuccess && courseInfo.isSuccess) {
                    val styleList = CourseCellStyleDBHelper.loadCourseCellStyle(courseInfo.data!!)

                    val todayAsync = async {
                        todayCourseArr = CourseUtils.getTodayCourse(courseInfo.data, termInfo.data!!)
                        if (todayCourseArr.isEmpty()) {
                            todayCourses.postValue(emptyArray())
                            nextCourseData.postValue(null)
                            if (showAttention) {
                                notifyMsg.postEventValue(CourseArrangeNotifyType.NO_TODAY_COURSE)
                            }
                            emptyArray()
                        } else {
                            val output = Array(todayCourseArr.size) {
                                CourseBundle(todayCourseArr[it], CourseStyleUtils.getStyleByCourseId(todayCourseArr[it].course.id, styleList)!!)
                            }
                            todayCourses.postValue(output)
                            val position = refreshNextCoursePosition(showAttention)
                            if (updateNextCourseWidget) {
                                val bundle = if (position != null) {
                                    ExtraCourseUtils.getNextCourseInfo(courseInfo.data, termInfo.data, output[position])
                                } else {
                                    ExtraCourseUtils.getNextCourseInfo(courseInfo.data, termInfo.data)
                                }
                                nextCourseBundle.postEventValue(bundle)
                            }
                            output
                        }
                    }

                    val tomorrowAsync = async {
                        tomorrowCourseArr = CourseUtils.getTomorrowCourse(courseInfo.data, termInfo.data!!)
                        if (tomorrowCourseArr.isEmpty()) {
                            tomorrowCourses.postValue(emptyArray())
                            emptyArray()
                        } else {
                            val output = Array(tomorrowCourseArr.size) {
                                CourseBundle(tomorrowCourseArr[it], CourseStyleUtils.getStyleByCourseId(tomorrowCourseArr[it].course.id, styleList)!!)
                            }
                            tomorrowCourses.postValue(output)
                            output
                        }
                    }

                    val notThisWeekAsync = async {
                        notThisWeekCourseArr = CourseUtils.getNotThisWeekCourse(courseInfo.data, termInfo.data!!)
                        if (notThisWeekCourseArr.isEmpty()) {
                            notThisWeekCourse.postValue(emptyArray())
                            emptyArray()
                        } else {
                            val output = Array(notThisWeekCourseArr.size) {
                                CourseBundle(
                                    notThisWeekCourseArr[it],
                                    CourseStyleUtils.getStyleByCourseId(notThisWeekCourseArr[it].course.id, styleList)!!
                                )
                            }
                            notThisWeekCourse.postValue(output)
                            output
                        }
                    }

                    val todayTemp = todayAsync.await()
                    val tomorrowTemp = tomorrowAsync.await()
                    val notThisWeekTemp = notThisWeekAsync.await()

                    CourseArrangeStore.saveStore(CourseArrange(todayTemp, tomorrowTemp, notThisWeekTemp, termInfo.data))
                } else {
                    todayCourses.postValue(emptyArray())
                    nextCourseData.postValue(null)
                    if (showAttention) {
                        notifyMsg.postEventValue(CourseArrangeNotifyType.COURSE_OR_TERM_DATA_EMPTY)
                    }
                    LogUtils.d<CourseArrangeViewModel>("Term Or Course Data Error! Term: ${termInfo.errorReason}  Course: ${courseInfo.errorReason}")
                }

                if (showAttention) {
                    isRefreshing.postValue(false)
                }
            }
        }
    }

    fun requestCourseDetail(courseItem: CourseItem, cellStyle: CourseCellStyle) {
        viewModelScope.launch(Dispatchers.Default) {
            if (::termDate.isInitialized) {
                courseDetail.postEventValue(
                    if (courseItem.detail == null) {
                        CourseDetail(
                            courseItem.course,
                            termDate,
                            cellStyle
                        )
                    } else {
                        CourseDetail(
                            courseItem.course,
                            termDate,
                            cellStyle,
                            CourseDetail.TimeDetail(
                                courseItem.courseTime.location,
                                courseItem.detail.weekDayNum,
                                termDate.currentWeekNum,
                                courseItem.detail.timePeriod
                            )
                        )
                    }
                )
            } else {
                notifyMsg.postEventValue(CourseArrangeNotifyType.LOADING_DATA)
            }
        }
    }

    private suspend fun refreshNextCoursePosition(showAttention: Boolean = false) = withContext(Dispatchers.Default) {
        if (::todayCourseArr.isInitialized) {
            val position = CourseUtils.getTodayNextCoursePosition(todayCourseArr)
            if (position != null) {
                nextCourseData.postValue(todayCourseArr[position])
            } else {
                nextCourseData.postValue(null)
            }
            return@withContext position
        } else {
            if (showAttention) {
                notifyMsg.postEventValue(CourseArrangeNotifyType.LOADING_DATA)
            }
        }
        return@withContext null
    }
}