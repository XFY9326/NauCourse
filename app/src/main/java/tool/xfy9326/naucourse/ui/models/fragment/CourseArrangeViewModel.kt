package tool.xfy9326.naucourse.ui.models.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.beans.CourseArrange
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.CourseDetail
import tool.xfy9326.naucourse.beans.CourseItem
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.info.methods.CourseInfo
import tool.xfy9326.naucourse.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourse.providers.store.CourseArrangeStore
import tool.xfy9326.naucourse.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourse.tools.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.compute.CourseUtils
import tool.xfy9326.naucourse.utils.debug.LogUtils

class CourseArrangeViewModel : BaseViewModel() {
    @Volatile
    private lateinit var todayCourseArr: Array<CourseItem>

    @Volatile
    private lateinit var tomorrowCourseArr: Array<CourseItem>

    @Volatile
    private lateinit var notThisWeekCourseArr: Array<Pair<Course, CourseTime>>

    @Volatile
    private lateinit var termDate: TermDate

    private lateinit var initJob: Job

    val nextCourseData = MutableLiveData<CourseItem?>()
    val isRefreshing = MutableLiveData<Boolean>()
    val todayCourses = MutableLiveData<Array<Pair<CourseItem, CourseCellStyle>>>()
    val tomorrowCourses = MutableLiveData<Array<Pair<CourseItem, CourseCellStyle>>>()
    val notThisWeekCourse = MutableLiveData<Array<Triple<Course, CourseTime, CourseCellStyle>>>()
    val notifyMsg = EventLiveData<CourseArrangeNotifyType>()
    val termDateData = MutableLiveData<TermDate?>()
    val courseDetail = EventLiveData<CourseDetail>()

    enum class CourseArrangeNotifyType {
        COURSE_OR_TERM_DATA_EMPTY,
        LOADING_DATA,
        NO_TODAY_COURSE
    }

    override fun onInitView(isRestored: Boolean) {
        if (!isRestored) initJob = loadInitCache()

        viewModelScope.launch {
            if (this@CourseArrangeViewModel::initJob.isInitialized) {
                if (initJob.isActive) initJob.join()
                refreshArrangeCourses()
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
                arrangeCache.todayCourseArr[it].first
            }
            refreshNextCoursePosition(false)

            tomorrowCourseArr = Array(arrangeCache.tomorrowCourseArr.size) {
                arrangeCache.tomorrowCourseArr[it].first
            }
            notThisWeekCourseArr = Array(arrangeCache.notThisWeekCourseArr.size) {
                Pair(arrangeCache.notThisWeekCourseArr[it].first, arrangeCache.notThisWeekCourseArr[it].second)
            }
        }
    }

    @Synchronized
    fun refreshArrangeCourses(showAttention: Boolean = false) {
        if (showAttention) {
            isRefreshing.postValue(true)
        }
        viewModelScope.launch(Dispatchers.Default) {
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
                val styleList = CourseCellStyleStore.loadCellStyles(courseInfo.data!!)

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
                            Pair(todayCourseArr[it], CourseCellStyle.getStyleByCourseId(todayCourseArr[it].course.id, styleList)!!)
                        }
                        todayCourses.postValue(output)
                        refreshNextCoursePosition(showAttention)
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
                            Pair(tomorrowCourseArr[it], CourseCellStyle.getStyleByCourseId(tomorrowCourseArr[it].course.id, styleList)!!)
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
                            Triple(
                                notThisWeekCourseArr[it].first, notThisWeekCourseArr[it].second,
                                CourseCellStyle.getStyleByCourseId(notThisWeekCourseArr[it].first.id, styleList)!!
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

    fun requestCourseDetail(courseItem: CourseItem, cellStyle: CourseCellStyle) {
        viewModelScope.launch(Dispatchers.Default) {
            if (::termDate.isInitialized) {
                courseDetail.postEventValue(
                    CourseDetail(
                        courseItem.course,
                        termDate,
                        cellStyle,
                        CourseDetail.TimeDetail(
                            courseItem.courseTime.location,
                            courseItem.weekDayNum,
                            termDate.currentWeekNum,
                            courseItem.timePeriod
                        )
                    )
                )
            } else {
                notifyMsg.postEventValue(CourseArrangeNotifyType.LOADING_DATA)
            }
        }
    }

    fun requestCourseDetail(course: Course, cellStyle: CourseCellStyle) {
        viewModelScope.launch(Dispatchers.Default) {
            if (::termDate.isInitialized) {
                courseDetail.postEventValue(
                    CourseDetail(
                        course,
                        termDate,
                        cellStyle
                    )
                )
            } else {
                notifyMsg.postEventValue(CourseArrangeNotifyType.LOADING_DATA)
            }
        }
    }

    private fun refreshNextCoursePosition(showAttention: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            if (::todayCourseArr.isInitialized) {
                val position = CourseUtils.getTodayNextCoursePosition(todayCourseArr)
                if (position != null) {
                    nextCourseData.postValue(todayCourseArr[position])
                    todayCourseArr[position]
                } else {
                    nextCourseData.postValue(null)
                }
            } else {
                if (showAttention) {
                    notifyMsg.postEventValue(CourseArrangeNotifyType.LOADING_DATA)
                }
            }
        }
    }
}