package tool.xfy9326.naucourses.ui.models.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.cache.CourseTableCache
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
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

    val maxWeekNum = MutableLiveData<Int>()
    val nowWeekNum = MutableLiveData<Int>()

    val courseTablePkg = Array<MutableLiveData<CoursePkg>>(Constants.Course.MAX_WEEK_NUM_SIZE) { MutableLiveData() }

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
        val courseInfo = CourseInfo.getInfo(loadCache = true)
        val termInfo = TermDateInfo.getInfo(loadCache = true)
        var courseTableArr = CourseTableCache.loadCache()
        if (termInfo.isSuccess) {
            termDate = termInfo.data!!
            val maxWeek = TimeUtils.getMaxWeekNum(termDate)
            maxWeekNum.postValue(maxWeek)
            nowWeekNum.postValue(TimeUtils.getWeekNum(termDate))
            if (courseInfo.isSuccess) {
                courseSet = courseInfo.data!!
                if (courseTableArr == null) {
                    courseTableArr = generateAllCourseTableCache(courseSet, maxWeek)
                    launch { CourseTableCache.saveCache(courseTableArr) }
                }
                this@CourseTableViewModel.courseTableArr = courseTableArr
                initSuccess = true
            }
        }
    }

    fun requestCourseTable(weekNum: Int, courseTableHash: Int) {
        viewModelScope.launch {
            initDeferred.await()
            if (initSuccess) {
                if (courseTableArr[weekNum - 1].hashCode() != courseTableHash) {
                    courseTablePkg[weekNum - 1].postValue(CoursePkg(termDate, courseTableArr[weekNum - 1]))
                }
            }
        }
    }

    fun updateCourseData(courseSet: CourseSet, termDate: TermDate) {
        if (!this::courseSet.isInitialized || !this::termDate.isInitialized || this.courseSet != courseSet || this.termDate != termDate) {
            this.courseSet = courseSet
            this.termDate = termDate
            viewModelScope.launch {
                val maxWeek = TimeUtils.getMaxWeekNum(termDate)
                maxWeekNum.postValue(maxWeek)
                nowWeekNum.postValue(TimeUtils.getWeekNum(termDate))
                courseTableArr = generateAllCourseTableCache(courseSet, maxWeek)
                launch { CourseTableCache.saveCache(courseTableArr) }

                for ((index, table) in courseTableArr.withIndex()) {
                    if (courseTablePkg[index].hasObservers()) {
                        courseTablePkg[index].postValue(CoursePkg(termDate, table))
                    }
                }
            }
        }
    }

    private suspend fun asyncCourseTable() = withContext(Dispatchers.Default) {
        val courseInfo = CourseInfo.getInfo(CourseInfo.OperationType.ASYNC_COURSE)
        val termInfo = TermDateInfo.getInfo()
        if (termInfo.isSuccess && courseInfo.isSuccess) {
            val termNeedUpdate = if (::termDate.isInitialized) {
                termDate != termInfo.data!!
            } else {
                true
            }
            val needUpdate = termNeedUpdate || !::courseSet.isInitialized || courseSet != courseInfo.data!!
            if (needUpdate) updateCourseData(courseInfo.data!!, termInfo.data!!)
        }
    }

    private suspend fun generateAllCourseTableCache(courseSet: CourseSet, maxWeekNum: Int): Array<CourseTable> {
        val result = arrayOfNulls<CourseTable>(maxWeekNum)
        val waitArr = arrayOfNulls<Deferred<CourseTable>>(maxWeekNum)
        return withContext(Dispatchers.Default) {
            coroutineScope {
                for (i in 0 until maxWeekNum) {
                    waitArr[i] = async {
                        CourseUtils.getCourseTableByWeekNum(courseSet, i + 1)
                    }
                }
                for (i in 0 until maxWeekNum) {
                    result[i] = waitArr[i]?.await()
                }
                result.requireNoNulls()
            }
        }
    }

    data class CoursePkg(
        val termDate: TermDate,
        val courseTable: CourseTable
    )
}
