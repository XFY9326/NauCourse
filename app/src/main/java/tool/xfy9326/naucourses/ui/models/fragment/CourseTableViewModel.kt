package tool.xfy9326.naucourses.ui.models.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CoursePkg
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.providers.store.CourseTableGsonStore
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.compute.CourseUtils
import tool.xfy9326.naucourses.utils.compute.TimeUtils

class CourseTableViewModel : BaseViewModel() {
    @Volatile
    private var hasInit = false
    private var initSuccess = false

    private lateinit var initDeferred: Deferred<Boolean>

    private lateinit var courseSet: CourseSet
    private lateinit var termDate: TermDate
    private lateinit var courseTableArr: Array<CourseTable>
    private lateinit var nowUsingStyles: Array<CourseCellStyle>

    var hasInitWithNowWeekNum = false

    var currentWeekNum: Int? = null
    var maxWeekNumTemp: Int? = null

    val maxWeekNum = MutableLiveData<Int>()
    val nowWeekNum = MutableLiveData<Int>()
    val nowShowWeekNum = MutableLiveData<Int>()
    val todayDate = MutableLiveData<Pair<Int, Int>>()
    val currentWeekStatus = MutableLiveData<CurrentWeekStatus>()
    val courseDetailInfo = MutableLiveData<Course>()

    val coursePkgSavedTemp = arrayOfNulls<CoursePkg>(Constants.Course.MAX_WEEK_NUM_SIZE)
    val courseTablePkg = Array<MutableLiveData<CoursePkg>>(Constants.Course.MAX_WEEK_NUM_SIZE) { MutableLiveData() }

    enum class CurrentWeekStatus {
        IS_CURRENT_WEEK,
        NOT_CURRENT_WEEK,
        IN_VACATION
    }

    override fun onActivityCreate() {
        viewModelScope.launch {
            val today = TimeUtils.getTodayDate()
            todayDate.postValue(today)
        }
    }

    fun initData() {
        if (!hasInit) {
            viewModelScope.launch {
                initDeferred = async {
                    initCourseData()
                    hasInit = true
                    true
                }
                launch {
                    asyncCourseTable()
                }
            }
        }
    }

    private suspend fun initCourseData() = withContext(Dispatchers.Default) {
        val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
        val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }
        val cacheCourseTableArrAsync = async { CourseTableGsonStore.loadStore() }
        val stylesAsync = async { CourseCellStyleStore.loadStore() }

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
                nowUsingStyles = CourseUtils.asyncCellStyle(courseInfo.data, stylesAsync.await())
                initSuccess = true
            } else {
                Log.d(this@CourseTableViewModel.javaClass.simpleName, "CourseInfo Init Error: ${courseInfo.errorReason}")
            }
        } else {
            Log.d(this@CourseTableViewModel.javaClass.simpleName, "TermInfo Init Error: ${termInfo.errorReason}")
        }
    }

    fun requestCourseTable(weekNum: Int, coursePkgHash: Int) {
        viewModelScope.launch {
            if (initDeferred.isActive) initDeferred.await()
            if (initSuccess) {
                if (courseTablePkg[weekNum - 1].hashCode() != coursePkgHash) {
                    courseTablePkg[weekNum - 1].postValue(
                        CoursePkg(
                            termDate,
                            courseTableArr[weekNum - 1],
                            nowUsingStyles
                        )
                    )
                }
            }
        }
    }

    fun requestCourseDetailInfo(courseId: String) {
        viewModelScope.launch {
            for (course in courseSet.courses) {
                if (course.id == courseId) {
                    courseDetailInfo.postValue(course)
                    break
                }
            }
        }
    }

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
                this.nowUsingStyles = styleList
                hasUpdateInfo = true
            }
            if (hasUpdateInfo) {
                viewModelScope.launch {
                    val termDatePostResult = postWeekInfoByTermDate(this@CourseTableViewModel.termDate)
                    currentWeekNum = termDatePostResult.second
                    launch {
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
    }

    private fun postWeekInfoByTermDate(termDate: TermDate): Pair<Int, Int> {
        val maxWeek = TimeUtils.getMaxWeekNum(termDate)
        val currentWeekNum = TimeUtils.getWeekNum(termDate)
        maxWeekNum.postValue(maxWeek)
        nowWeekNum.postValue(currentWeekNum)
        return Pair(maxWeek, currentWeekNum)
    }

    private fun validateUpdateNecessary(courseSet: CourseSet? = null, termDate: TermDate? = null, styleList: Array<CourseCellStyle>? = null) =
        !this::courseSet.isInitialized || !this::termDate.isInitialized || this.courseSet != courseSet || this.termDate != termDate ||
                !this::nowUsingStyles.isInitialized || (styleList != null && !this.nowUsingStyles.contentEquals(styleList))

    fun requestShowWeekStatus(nowShowWeekNum: Int) {
        viewModelScope.launch {
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
        val courseInfoAsync = async { CourseInfo.getInfo(CourseInfo.OperationType.ASYNC_COURSE) }
        val termInfoAsync = async { TermDateInfo.getInfo() }

        val termInfo = termInfoAsync.await()
        if (termInfo.isSuccess) {
            val termNeedUpdate = if (::termDate.isInitialized) {
                termDate != termInfo.data!!
            } else {
                true
            }
            val courseInfo = courseInfoAsync.await()
            if (courseInfo.isSuccess) {
                val needUpdate = termNeedUpdate || !::courseSet.isInitialized || courseSet != courseInfo.data!!
                if (needUpdate) {
                    val styles = CourseUtils.asyncCellStyle(courseInfo.data!!, if (::nowUsingStyles.isInitialized) nowUsingStyles else null)
                    updateCourseData(courseInfo.data, termInfo.data!!, styles)
                } else {
                    Log.i(this@CourseTableViewModel.javaClass.simpleName, "Course Async Don't Need Update")
                }
            } else {
                Log.d(this@CourseTableViewModel.javaClass.simpleName, "CourseInfo Async Error: ${courseInfo.errorReason}")
            }
        } else {
            Log.d(this@CourseTableViewModel.javaClass.simpleName, "TermInfo Async Error: ${termInfo.errorReason}")
        }
    }

    private suspend fun generateAllCourseTable(courseSet: CourseSet, termDate: TermDate, maxWeekNum: Int, postValue: Boolean = false) {
        val result = arrayOfNulls<CourseTable>(maxWeekNum)
        val waitArr = arrayOfNulls<Deferred<CourseTable>>(maxWeekNum)
        courseTableArr = withContext(Dispatchers.Default) {
            for (i in 0 until maxWeekNum) {
                waitArr[i] = async {
                    val weekCourseTable = CourseUtils.getCourseTableByWeekNum(courseSet, i + 1)
                    if (postValue && courseTablePkg[i].hasActiveObservers()) courseTablePkg[i].postValue(
                        CoursePkg(
                            termDate,
                            weekCourseTable,
                            nowUsingStyles
                        )
                    )
                    weekCourseTable
                }
            }
            for (i in 0 until maxWeekNum) {
                result[i] = waitArr[i]?.await()
            }
            val table = result.requireNoNulls()
            launch { CourseTableGsonStore.saveStore(table) }
            table
        }
    }
}
