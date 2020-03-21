package tool.xfy9326.naucourses.ui.models.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.beans.*
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.providers.store.CourseTableStore
import tool.xfy9326.naucourses.tools.EventLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.compute.CourseUtils
import tool.xfy9326.naucourses.utils.compute.TimeUtils
import tool.xfy9326.naucourses.utils.debug.LogUtils

class CourseTableViewModel : BaseViewModel() {
    companion object {
        const val DEFAULT_COURSE_PKG_HASH = 0
    }

    @Volatile
    private var hasInit = false

    private lateinit var initDeferred: Deferred<Boolean>

    private lateinit var courseSet: CourseSet
    private lateinit var termDate: TermDate
    private lateinit var courseTableArr: Array<CourseTable>

    var hasInitWithNowWeekNum = false

    var currentWeekNum: Int? = null
    var maxWeekNumTemp: Int? = null

    val maxWeekNum = MutableLiveData<Int>()
    val nowWeekNum = MutableLiveData<Int>()
    val nowShowWeekNum = MutableLiveData<Int>()
    val todayDate = MutableLiveData<Pair<Int, Int>>()
    val currentWeekStatus = MutableLiveData<CurrentWeekStatus>()
    val courseDetailInfo = EventLiveData<CourseDetail>()
    val courseAndTermEmpty = EventLiveData<Boolean>()

    val coursePkgSavedTemp = arrayOfNulls<CoursePkg>(Constants.Course.MAX_WEEK_NUM_SIZE)
    val courseTablePkg = Array<MutableLiveData<CoursePkg>>(Constants.Course.MAX_WEEK_NUM_SIZE) { MutableLiveData() }

    enum class CurrentWeekStatus {
        IS_CURRENT_WEEK,
        NOT_CURRENT_WEEK,
        IN_VACATION
    }

    override fun onInitView(isRestored: Boolean) {
        if (!isRestored) {
            viewModelScope.launch(Dispatchers.Default) {
                val today = TimeUtils.getTodayDate()
                todayDate.postValue(today)
            }
        }
    }

    override fun onInitCache(isRestored: Boolean) {
        if (!isRestored) {
            if (!hasInit) {
                initDeferred = viewModelScope.async(Dispatchers.Default) {
                    initCourseData()
                    hasInit = true
                    true
                }
                viewModelScope.launch(Dispatchers.Default) {
                    asyncCourseTable()
                }
            }
        }
    }

    private suspend fun initCourseData() = withContext(Dispatchers.Default) {
        val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
        val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }
        val cacheCourseTableArrAsync = async { CourseTableStore.loadStore() }

        val termInfo = termInfoAsync.await()
        if (termInfo.isSuccess) {
            termDate = termInfo.data!!
            val termDatePostResult = postWeekInfoByTermDate(termInfo.data)
            currentWeekNum = termDatePostResult.second
            val courseInfo = courseInfoAsync.await()
            if (courseInfo.isSuccess) {
                courseSet = courseInfo.data!!
                val cacheCourseTableArr = cacheCourseTableArrAsync.await()
                if (cacheCourseTableArr == null) {
                    makeCourseTable(courseSet, termDate, termDatePostResult.first)
                } else {
                    courseTableArr = cacheCourseTableArr
                }
            } else {
                LogUtils.d<CourseTableViewModel>("CourseInfo Init Error: ${courseInfo.errorReason}")
            }
        } else {
            LogUtils.d<CourseTableViewModel>("TermInfo Init Error: ${termInfo.errorReason}")
        }
    }

    @Synchronized
    fun requestCourseTable(weekNum: Int, coursePkgHash: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            if (initDeferred.isActive) initDeferred.await()
            if (coursePkgHash == DEFAULT_COURSE_PKG_HASH || coursePkgSavedTemp[weekNum - 1].hashCode() != coursePkgHash) {
                if (::termDate.isInitialized && ::courseTableArr.isInitialized && ::courseSet.isInitialized) {
                    val pkg = CoursePkg(
                        termDate,
                        courseTableArr[weekNum - 1],
                        CourseCellStyleStore.loadCellStyles(courseSet)
                    )
                    coursePkgSavedTemp[weekNum - 1] = pkg
                    courseTablePkg[weekNum - 1].postValue(pkg)
                } else if (::termDate.isInitialized && (!::courseTableArr.isInitialized || !::courseSet.isInitialized)) {
                    val pkg = CoursePkg(
                        termDate,
                        CourseTable(emptyArray()),
                        emptyArray()
                    )
                    coursePkgSavedTemp[weekNum - 1] = pkg
                    courseTablePkg[weekNum - 1].postValue(pkg)

                    LogUtils.i<CourseTableViewModel>("Init Empty Course Data For Week: $weekNum!")
                } else {
                    courseAndTermEmpty.postEventValue(true)

                    LogUtils.d<CourseTableViewModel>("Init Request Failed For Week: $weekNum!")
                }
            } else {
                LogUtils.d<CourseTableViewModel>("Course Update Unnecessary For Week: $weekNum!")
            }
        }
    }

    @Synchronized
    fun requestCourseDetailInfo(courseCell: CourseCell, cellStyle: CourseCellStyle) {
        viewModelScope.launch(Dispatchers.Default) {
            for (course in courseSet.courses) {
                if (course.id == courseCell.courseId) {
                    courseDetailInfo.postEventValue(
                        CourseDetail(
                            course,
                            termDate,
                            cellStyle,
                            CourseDetail.TimeDetail(
                                courseCell.courseLocation,
                                courseCell.weekDayNum,
                                courseCell.weekNum,
                                CourseTimeDuration.convertToTimePeriod(courseCell.timeDuration)
                            )
                        )
                    )
                    break
                }
            }
        }
    }

    fun refreshCourseData() {
        viewModelScope.launch(Dispatchers.Default) {
            val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
            val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }

            val termInfo = termInfoAsync.await()
            val termDate = if (termInfo.isSuccess) {
                termInfo.data!!
            } else {
                LogUtils.d<CourseTableViewModel>("TermInfo Update Error: ${termInfo.errorReason}")
                null
            }

            val courseInfo = courseInfoAsync.await()
            val courseData = if (courseInfo.isSuccess) {
                courseInfo.data!!
            } else {
                LogUtils.d<CourseTableViewModel>("CourseInfo Update Error: ${courseInfo.errorReason}")
                null
            }

            val styles = if (courseData != null) {
                CourseCellStyleStore.loadCellStyles(courseData)
            } else {
                null
            }

            hasInitWithNowWeekNum = false
            updateCourseData(courseData, termDate, styles)
        }
    }

    @Synchronized
    private fun updateCourseData(courseSet: CourseSet? = null, termDate: TermDate? = null, styleList: Array<CourseCellStyle>? = null) {
        if (validateUpdateNecessary(courseSet, termDate, styleList)) {
            var hasUpdateInfo = false
            if (courseSet != null) {
                this.courseSet = courseSet
                hasUpdateInfo = true
            }
            if (termDate != null) {
                this.termDate = termDate
                hasUpdateInfo = true
            }
            if (styleList != null) {
                CourseCellStyleStore.saveStore(styleList)
                hasUpdateInfo = true
            }
            if (hasUpdateInfo) {
                viewModelScope.launch(Dispatchers.Default) {
                    val termDatePostResult = postWeekInfoByTermDate(this@CourseTableViewModel.termDate)
                    currentWeekNum = termDatePostResult.second
                    makeCourseTable(
                        this@CourseTableViewModel.courseSet,
                        this@CourseTableViewModel.termDate,
                        termDatePostResult.first,
                        true
                    )
                }
            }
        }
    }

    private fun postWeekInfoByTermDate(termDate: TermDate): Pair<Int, Int> {
        val maxWeek = TimeUtils.getWeekLength(termDate)
        val currentWeekNum = TimeUtils.getWeekNum(termDate)
        maxWeekNum.postValue(maxWeek)
        nowWeekNum.postValue(currentWeekNum)
        return Pair(maxWeek, currentWeekNum)
    }

    private fun validateUpdateNecessary(
        courseSet: CourseSet? = null,
        termDate: TermDate? = null,
        styleList: Array<CourseCellStyle>? = null
    ): Boolean {
        val storedStyle = CourseCellStyleStore.loadStore()
        return !this::courseSet.isInitialized || !this::termDate.isInitialized || this.courseSet != courseSet || this.termDate != termDate ||
                storedStyle == null || (styleList != null && storedStyle.contentEquals(styleList))
    }

    fun requestShowWeekStatus(nowShowWeekNum: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            currentWeekStatus.postValue(
                if (currentWeekNum == 0) {
                    CurrentWeekStatus.IN_VACATION
                } else {
                    if (currentWeekNum == nowShowWeekNum) {
                        CurrentWeekStatus.IS_CURRENT_WEEK
                    } else {
                        CurrentWeekStatus.NOT_CURRENT_WEEK
                    }
                }
            )
        }
    }

    private suspend fun asyncCourseTable() = withContext(Dispatchers.Default) {
        val termInfo = TermDateInfo.getInfo()
        if (termInfo.isSuccess) {
            val termNeedUpdate = if (::termDate.isInitialized) {
                termDate != termInfo.data!!
            } else {
                true
            }
            if (termNeedUpdate) {
                val courseInfo = CourseInfo.getInfo(CourseInfo.OperationType.ASYNC_COURSE)
                if (courseInfo.isSuccess) {
                    val styles = CourseCellStyleStore.loadCellStyles(courseInfo.data!!)
                    updateCourseData(courseInfo.data, termInfo.data!!, styles)
                } else {
                    LogUtils.d<CourseTableViewModel>("CourseInfo Async Error: ${courseInfo.errorReason}")
                }
            } else {
                LogUtils.d<CourseTableViewModel>("Course Async Don't Need Update (Same Term Date)")
            }
        } else {
            LogUtils.d<CourseTableViewModel>("Term Date Async Error: ${termInfo.errorReason}")
        }
    }

    private suspend fun makeCourseTable(courseSet: CourseSet, termDate: TermDate, maxWeekNum: Int, postValue: Boolean = false) {
        withContext(Dispatchers.Default) {
            courseTableArr = CourseUtils.generateAllCourseTable(courseSet, termDate, maxWeekNum)
            if (postValue) {
                for (i in courseTablePkg.indices) {
                    if (courseTablePkg[i].hasActiveObservers()) {
                        courseTablePkg[i].postValue(
                            CoursePkg(
                                termDate,
                                courseTableArr[i],
                                CourseCellStyleStore.loadCellStyles(courseSet)
                            )
                        )
                    }
                }
            }
        }
    }
}
