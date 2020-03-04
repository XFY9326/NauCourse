package tool.xfy9326.naucourses.ui.models.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseDetail
import tool.xfy9326.naucourses.beans.CourseItem
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.tools.EventLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.LogUtils
import tool.xfy9326.naucourses.utils.compute.CourseUtils

class CourseArrangeViewModel : BaseViewModel() {
    @Volatile
    private lateinit var todayCourseArr: Array<CourseItem>
    @Volatile
    private lateinit var tomorrowCourseArr: Array<CourseItem>
    @Volatile
    private lateinit var notThisWeekCourseArr: Array<Pair<Course, CourseTime>>

    @Volatile
    private lateinit var termDate: TermDate

    val nextCourse = MutableLiveData<CourseItem?>()
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
        refreshArrangeCourses()
        refreshNextCoursePosition()
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
                        nextCourse.postValue(null)
                        if (showAttention) {
                            notifyMsg.postEventValue(CourseArrangeNotifyType.NO_TODAY_COURSE)
                        }
                    } else {
                        val output = Array(todayCourseArr.size) {
                            Pair(todayCourseArr[it], CourseCellStyle.getStyleByCourseId(todayCourseArr[it].course.id, styleList)!!)
                        }
                        todayCourses.postValue(output)
                        refreshNextCoursePosition(showAttention)
                    }
                }

                val tomorrowAsync = async {
                    tomorrowCourseArr = CourseUtils.getTomorrowCourse(courseInfo.data, termInfo.data!!)
                    if (tomorrowCourseArr.isEmpty()) {
                        tomorrowCourses.postValue(emptyArray())
                    } else {
                        val output = Array(tomorrowCourseArr.size) {
                            Pair(tomorrowCourseArr[it], CourseCellStyle.getStyleByCourseId(tomorrowCourseArr[it].course.id, styleList)!!)
                        }
                        tomorrowCourses.postValue(output)
                    }
                }

                val notThisWeekAsync = async {
                    notThisWeekCourseArr = CourseUtils.getNotThisWeekCourse(courseInfo.data, termInfo.data!!)
                    if (notThisWeekCourseArr.isEmpty()) {
                        notThisWeekCourse.postValue(emptyArray())
                    } else {
                        val output = Array(notThisWeekCourseArr.size) {
                            Triple(
                                notThisWeekCourseArr[it].first, notThisWeekCourseArr[it].second,
                                CourseCellStyle.getStyleByCourseId(notThisWeekCourseArr[it].first.id, styleList)!!
                            )
                        }
                        notThisWeekCourse.postValue(output)
                    }
                }

                todayAsync.await()
                tomorrowAsync.await()
                notThisWeekAsync.await()
            } else {
                todayCourses.postValue(emptyArray())
                nextCourse.postValue(null)
                if (showAttention) {
                    notifyMsg.postEventValue(CourseArrangeNotifyType.COURSE_OR_TERM_DATA_EMPTY)
                }
                LogUtils.d<CourseArrangeViewModel>("Term Or Course Data Empty! Term: ${termInfo.errorReason}  Course: ${courseInfo.errorReason}")
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
                        courseItem.courseTime.location,
                        courseItem.weekDayNum,
                        termDate.currentWeekNum,
                        courseItem.timePeriod,
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
                    nextCourse.postValue(todayCourseArr[position])
                } else {
                    nextCourse.postValue(null)
                }
            } else {
                if (showAttention) {
                    notifyMsg.postEventValue(CourseArrangeNotifyType.LOADING_DATA)
                }
            }
        }
    }
}