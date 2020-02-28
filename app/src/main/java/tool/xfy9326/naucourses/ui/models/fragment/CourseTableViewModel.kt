package tool.xfy9326.naucourses.ui.models.fragment

import android.util.Log
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
import tool.xfy9326.naucourses.tools.SingleLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.compute.CourseUtils
import tool.xfy9326.naucourses.utils.compute.TimeUtils

class CourseTableViewModel : BaseViewModel() {
    private val logTag = javaClass.simpleName

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
    val courseDetailInfo = SingleLiveData<CourseDetail>()

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
                    generateAllCourseTable(courseSet, termDate, termDatePostResult.first)
                } else {
                    courseTableArr = cacheCourseTableArr
                }
            } else {
                Log.d(logTag, "CourseInfo Init Error: ${courseInfo.errorReason}")
            }
        } else {
            Log.d(logTag, "TermInfo Init Error: ${termInfo.errorReason}")
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
                } else {
                    Log.d(logTag, "Init Request Failed For Week: $weekNum!")
                }
            } else {
                Log.d(logTag, "Course Update Unnecessary For Week: $weekNum!")
            }
        }
    }

    @Synchronized
    fun requestCourseDetailInfo(courseCell: CourseCell, cellStyle: CourseCellStyle) {
        viewModelScope.launch(Dispatchers.Default) {
            for (course in courseSet.courses) {
                if (course.id == courseCell.courseId) {
                    courseDetailInfo.postSingleValue(CourseDetail(course, termDate, courseCell, cellStyle))
                    break
                }
            }
        }
    }

    @Synchronized
    fun updateCourseData(courseSet: CourseSet? = null, termDate: TermDate? = null, styleList: Array<CourseCellStyle>? = null) {
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
                    generateAllCourseTable(
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
        val maxWeek = TimeUtils.getMaxWeekNum(termDate)
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
                    Log.d(logTag, "CourseInfo Async Error: ${courseInfo.errorReason}")
                }
            } else {
                Log.i(logTag, "Course Async Don't Need Update (Same Term Date)")
            }
        } else {
            Log.d(logTag, "Term Date Async Error: ${termInfo.errorReason}")
        }
    }

    private suspend fun generateAllCourseTable(courseSet: CourseSet, termDate: TermDate, maxWeekNum: Int, postValue: Boolean = false) {
        withContext(Dispatchers.Default) {
            val result = arrayOfNulls<CourseTable>(maxWeekNum)
            val waitArr = arrayOfNulls<Deferred<CourseTable>>(maxWeekNum)

            val startWeekDayNum = TimeUtils.getWeekDayNum(termDate.startDate)
            val endWeekDayNum = TimeUtils.getWeekDayNum(termDate.endDate)
            for (i in 0 until maxWeekNum) {
                waitArr[i] = async {
                    val weekTable = CourseUtils.getCourseTableByWeekNum(courseSet, i + 1, maxWeekNum, startWeekDayNum, endWeekDayNum)
                    if (postValue && courseTablePkg[i].hasActiveObservers()) {
                        courseTablePkg[i].postValue(
                            CoursePkg(
                                termDate,
                                weekTable,
                                CourseCellStyleStore.loadCellStyles(courseSet)
                            )
                        )
                    }
                    weekTable
                }
            }
            for (i in 0 until maxWeekNum) {
                result[i] = waitArr[i]?.await()
            }
            courseTableArr = result.requireNoNulls()
            launch { CourseTableStore.saveStore(courseTableArr) }
        }
    }
}
