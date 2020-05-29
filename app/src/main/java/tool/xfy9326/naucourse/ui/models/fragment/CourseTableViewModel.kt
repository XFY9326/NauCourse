package tool.xfy9326.naucourse.ui.models.fragment

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.*
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.io.store.CourseTableStore
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.info.methods.CourseInfo
import tool.xfy9326.naucourse.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.ui.views.table.CourseTableStyle
import tool.xfy9326.naucourse.ui.views.table.CourseTableViewHelper
import tool.xfy9326.naucourse.utils.BaseUtils.tryWithLock
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.utility.BitmapUtils
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import java.lang.ref.WeakReference
import kotlin.math.min

class CourseTableViewModel : BaseViewModel() {
    private lateinit var initDeferred: Deferred<Boolean>

    private lateinit var courseSet: CourseSet
    private lateinit var termDate: TermDate
    private lateinit var courseTableArr: Array<CourseTable>
    private val hasTableInit = BooleanArray(Constants.Course.MAX_WEEK_NUM_SIZE) { false }

    @Volatile
    private var courseTableStyle: CourseTableStyle? = null
    private val courseTableStyleLock = Any()

    private val courseTableAsyncLock = Mutex()

    var hasInitWithNowWeekNum = false

    var showNextWeekAhead: Boolean? = null
    var currentWeekNum: Int? = null

    val imageOperation = EventLiveData<ImageOperationType>()
    val imageShareUri = EventLiveData<Uri>()

    val maxWeekNum = MutableLiveData<Int>()

    // 当前周数，是否要提前显示下一周课表
    val nowWeekNum = MutableLiveData<Pair<Int, Boolean>>()

    val nowShowWeekNum = MutableLiveData<Int>()
    val todayDate = MutableLiveData<Pair<Int, Int>>()
    val currentWeekStatus = MutableLiveData<CurrentWeekStatus>()
    val courseDetailInfo = EventLiveData<CourseDetail>()
    val courseAndTermEmpty = NotifyLivaData()

    val getImageWhenCourseTableLoading = NotifyLivaData()

    val courseTablePkg = Array<MutableLiveData<CoursePkg>>(Constants.Course.MAX_WEEK_NUM_SIZE) { MutableLiveData() }

    enum class CurrentWeekStatus {
        IS_CURRENT_WEEK,
        IS_NEXT_WEEK,
        NOT_CURRENT_WEEK,
        IN_VACATION
    }

    override fun onFragmentAttach() {
        viewModelScope.launch {
            initTableCache()
        }
    }

    override fun onInitCache(isRestored: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            todayDate.postValue(TimeUtils.getTodayDate())
        }
    }

    private suspend fun initTableCache() = withContext(Dispatchers.Default) {
        if (tryInit()) {
            initDeferred = viewModelScope.async(Dispatchers.Default) {
                initCourseData()
                true
            }
            startOnlineDataAsync()
        }
    }

    private suspend fun initCourseData() = withContext(Dispatchers.Default) {
        val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
        val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }
        val cacheCourseTableArrAsync = async { CourseTableStore.loadStore() }

        val termInfo = termInfoAsync.await()
        if (termInfo.isSuccess) {
            termDate = termInfo.data!!
            val courseInfo = courseInfoAsync.await()
            if (courseInfo.isSuccess) {
                courseSet = courseInfo.data!!
                val cacheCourseTableArr = cacheCourseTableArrAsync.await()
                // 课表的缓存有效期为一天
                if (cacheCourseTableArr == null ||
                    System.currentTimeMillis() - (cacheCourseTableArr.firstOrNull()?.saveDate ?: 0) > 1000 * 60 * 60 * 24
                ) {
                    makeCourseTable(courseSet, termDate)
                } else {
                    courseTableArr = cacheCourseTableArr
                }
            } else {
                LogUtils.d<CourseTableViewModel>("CourseInfo Init Error: ${courseInfo.errorReason}")
            }
            setWeekInfoByTermDate(termInfo.data, courseInfo.data)
        } else {
            LogUtils.d<CourseTableViewModel>("TermInfo Init Error: ${termInfo.errorReason}")
        }
    }

    fun startOnlineDataAsync() {
        if (SettingsPref.AutoAsyncCourseData) {
            viewModelScope.launch(Dispatchers.Default) {
                if (CourseInfo.isCacheExpired()) {
                    if (::initDeferred.isInitialized) {
                        if (initDeferred.isActive) initDeferred.await()
                        asyncCourseTable()
                    } else {
                        initTableCache()
                        startOnlineDataAsync()
                    }
                }
            }
        }
    }

    fun requestCourseTable(weekNum: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            if (::initDeferred.isInitialized) {
                if (initDeferred.isActive) initDeferred.await()
                getCoursePkg(weekNum)?.let {
                    courseTablePkg[weekNum - 1].postValue(it)
                }
            } else {
                initTableCache()
                requestCourseTable(weekNum)
            }
        }
    }

    private fun getCoursePkg(weekNum: Int) =
        if (::termDate.isInitialized && ::courseTableArr.isInitialized && ::courseSet.isInitialized) {
            val table = courseTableArr[weekNum - 1]
            val style = getCourseTableStyle()
            val pkg = CoursePkg(
                termDate,
                table,
                CourseCellStyleDBHelper.loadCourseCellStyle(courseSet),
                style
            )
            pkg
        } else if (::termDate.isInitialized && (!::courseTableArr.isInitialized || !::courseSet.isInitialized)) {
            val pkg = CoursePkg(
                termDate,
                CourseTable(emptyArray()),
                emptyArray(),
                getCourseTableStyle()
            )
            LogUtils.i<CourseTableViewModel>("Init Empty Course Data For Week: $weekNum!")
            pkg
        } else {
            courseAndTermEmpty.notifyEvent()
            LogUtils.d<CourseTableViewModel>("Init Request Failed For Week: $weekNum!")
            null
        }

    @Synchronized
    fun requestCourseDetailInfo(courseCell: CourseCell, cellStyle: CourseCellStyle) {
        viewModelScope.launch(Dispatchers.Default) {
            for (course in courseSet.courses) {
                if (course.id == courseCell.courseId) {
                    courseDetailInfo.postEventValue(
                        if (courseCell.thisWeekCourse) {
                            CourseDetail(
                                course,
                                termDate,
                                cellStyle,
                                CourseDetail.TimeDetail(
                                    courseCell.courseTime.location,
                                    courseCell.courseTime.weekDay,
                                    courseCell.weekNum,
                                    TimeUtils.convertToTimePeriod(courseCell.timeDuration)
                                )
                            )
                        } else {
                            CourseDetail(
                                course,
                                termDate,
                                cellStyle
                            )
                        }
                    )
                    break
                }
            }
        }
    }

    fun refreshTimeInfo() {
        viewModelScope.launch(Dispatchers.Default) {
            val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }
            val termInfo = termInfoAsync.await()
            if (termInfo.isSuccess) {
                hasInitWithNowWeekNum = false
                setWeekInfoByTermDate(
                    termInfo.data!!,
                    if (this@CourseTableViewModel::courseSet.isInitialized) this@CourseTableViewModel.courseSet else null
                )
            } else {
                LogUtils.d<CourseTableViewModel>("TermInfo Refresh Error: ${termInfo.errorReason}")
            }
        }
    }

    fun refreshCourseData(forceUpdate: Boolean = false) {
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
                CourseCellStyleDBHelper.loadCourseCellStyle(courseData)
            } else {
                null
            }

            hasInitWithNowWeekNum = false
            updateCourseData(courseData, termDate, styles, forceUpdate)
        }
    }

    @Synchronized
    private fun updateCourseData(
        courseSet: CourseSet? = null, termDate: TermDate? = null, styleList: Array<CourseCellStyle>? = null,
        forceUpdate: Boolean = false
    ) {
        var hasTermUpdateInfo = false
        var hasCourseUpdateInfo = false
        if (styleList != null) {
            hasCourseUpdateInfo = true
        }
        if (courseSet != null && (!this::courseSet.isInitialized || this.courseSet != courseSet)) {
            this.courseSet = courseSet
            hasCourseUpdateInfo = true
        }
        if (termDate != null && (!this::termDate.isInitialized || this.termDate != termDate)) {
            this.termDate = termDate
            hasTermUpdateInfo = true
        }
        if (hasCourseUpdateInfo || hasTermUpdateInfo || forceUpdate) {
            viewModelScope.launch(Dispatchers.Default) {
                if (hasCourseUpdateInfo || forceUpdate) {
                    makeCourseTable(this@CourseTableViewModel.courseSet, this@CourseTableViewModel.termDate, true)
                }
                if (hasTermUpdateInfo || forceUpdate) {
                    setWeekInfoByTermDate(
                        this@CourseTableViewModel.termDate,
                        if (this@CourseTableViewModel::courseSet.isInitialized) this@CourseTableViewModel.courseSet else null
                    )
                }
            }
        }
    }

    // 应当在生成课表后调用以提高加载速度
    @Synchronized
    private suspend fun setWeekInfoByTermDate(termDate: TermDate, courseSet: CourseSet?) {
        val maxWeek = TimeUtils.getWeekLength(termDate)
        val currentWeekNum = TimeUtils.getWeekNum(termDate)
        val showAhead =
            if (courseSet != null && courseSet.hasCourse &&
                SettingsPref.ShowNextWeekCourseTableAhead && TimeUtils.inWeekend() && currentWeekNum < maxWeek
            ) {
                val currentWeekTable =
                    if (::courseTableArr.isInitialized) {
                        courseTableArr[currentWeekNum - 1]
                    } else {
                        makeCourseTable(courseSet, termDate)
                        courseTableArr[currentWeekNum - 1]
                    }
                !CourseUtils.hasWeekendCourse(currentWeekTable)
            } else {
                false
            }
        maxWeekNum.postValue(maxWeek)
        if (currentWeekNum != this.currentWeekNum || showNextWeekAhead != showAhead) {
            this.currentWeekNum = currentWeekNum
            this.showNextWeekAhead = showAhead
            nowWeekNum.postValue(Pair(currentWeekNum, showAhead))
        }
    }

    fun requestShowWeekStatus(nowShowWeekNum: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            currentWeekStatus.postValue(
                when (currentWeekNum) {
                    0 -> CurrentWeekStatus.IN_VACATION
                    nowShowWeekNum -> CurrentWeekStatus.IS_CURRENT_WEEK
                    nowShowWeekNum - 1 -> CurrentWeekStatus.IS_NEXT_WEEK
                    else -> CurrentWeekStatus.NOT_CURRENT_WEEK
                }
            )
        }
    }

    private suspend fun asyncCourseTable() = withContext(Dispatchers.Default) {
        courseTableAsyncLock.tryWithLock {
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
                        val courseDataNeedUpdate = if (::courseSet.isInitialized) {
                            courseSet != courseInfo.data!!
                        } else {
                            true
                        }
                        if (courseDataNeedUpdate) {
                            val styles = CourseCellStyleDBHelper.loadCourseCellStyle(courseInfo.data!!)
                            updateCourseData(courseInfo.data, termInfo.data!!, styles)
                            NotifyBus[NotifyBus.Type.COURSE_ASYNC_UPDATE].notifyEvent()
                        }
                    } else {
                        LogUtils.d<CourseTableViewModel>("CourseInfo Async Error: ${courseInfo.errorReason}")
                    }
                }
            } else {
                LogUtils.d<CourseTableViewModel>("Term Date Async Error: ${termInfo.errorReason}")
            }
        }
    }

    private suspend fun makeCourseTable(courseSet: CourseSet, termDate: TermDate, postValue: Boolean = false) = withContext(Dispatchers.Default) {
        courseTableArr = CourseUtils.generateAllCourseTable(courseSet, termDate, TimeUtils.getWeekLength(termDate))
        if (postValue) {
            val style = getCourseTableStyle()
            for (i in 0 until min(courseTablePkg.size, courseTableArr.size)) {
                val table = courseTableArr[i]
                courseTablePkg[i].postValue(
                    CoursePkg(
                        termDate,
                        table,
                        CourseCellStyleDBHelper.loadCourseCellStyle(courseSet),
                        style
                    )
                )
            }
        }
    }

    fun rebuildCourseTable() {
        viewModelScope.launch(Dispatchers.Default) {
            val newStyle = getCourseTableStyle(true)
            for (liveData in courseTablePkg) {
                liveData.value?.let {
                    liveData.postValue(it.copy(courseTableStyle = newStyle))
                }
            }
        }
    }

    private fun getCourseTableStyle(forceRefresh: Boolean = false) = synchronized(courseTableStyleLock) {
        if (courseTableStyle == null || forceRefresh) {
            courseTableStyle = CourseTableViewHelper.getCourseTableStyle()
        }
        return@synchronized courseTableStyle!!
    }

    fun createShareImage(context: Context, weekNum: Int, targetWidth: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val weakContext = WeakReference(context)
            if (hasInit()) {
                val pkg = getCoursePkg(weekNum)
                if (pkg == null) {
                    getImageWhenCourseTableLoading.notifyEvent()
                } else {
                    weakContext.get()?.let {
                        CourseTableViewHelper.drawCourseTableImage(it, pkg, weekNum, targetWidth).let { bitmap ->
                            BitmapUtils.drawDefaultWaterPrint(it, bitmap)
                            shareCourseTable(bitmap)
                        }
                    }
                }
            } else {
                getImageWhenCourseTableLoading.notifyEvent()
            }
        }
    }

    private suspend fun shareCourseTable(bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val uri = ImageUtils.createImageShareTemp(null, bitmap, true)
        if (uri == null) {
            imageOperation.postEventValue(ImageOperationType.IMAGE_SHARE_FAILED)
        } else {
            imageShareUri.postEventValue(uri)
        }
    }

    @Synchronized
    fun tryInitTable(weekNum: Int) =
        if (hasTableInit[weekNum - 1]) {
            false
        } else {
            hasTableInit[weekNum - 1] = true
            true
        }
}
