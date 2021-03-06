package tool.xfy9326.naucourse.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.CourseManagePkg
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import tool.xfy9326.naucourse.kt.tryWithLock
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.Term
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.info.methods.CourseInfo
import tool.xfy9326.naucourse.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.courses.CourseStyleUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.debug.LogUtils

class CourseManageViewModel : BaseViewModel() {
    val rawTermDate = EventLiveData<TermDate>()

    // 存在第三个时前两个空，否则不为空
    val importCourseResult = EventLiveData<Triple<CourseSet?, ImportCourseType?, ContentErrorReason?>>()
    val courseManagePkg = MutableLiveData<CourseManagePkg>()
    val saveSuccess = NotifyLivaData()
    val onlineCourseConflict = NotifyLivaData()

    var dataChanged = false
        private set

    fun setDataChanged() {
        dataChanged = true
    }

    var colorEditPosition: Int? = null
    var colorEditStyle: CourseCellStyle? = null

    var requireCleanTermDate: Boolean = false

    private val saveLock = Mutex()
    private val importLock = Mutex()

    override fun onInitView(isRestored: Boolean) {
        tryInit { requestCourseManagePkg() }
    }

    enum class ImportCourseType {
        CURRENT_TERM,
        NEXT_TERM
    }

    private fun requestCourseManagePkg() {
        viewModelScope.launch(Dispatchers.Default) {
            val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
            val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }

            val termInfo = termInfoAsync.await()
            val courseInfo = courseInfoAsync.await()

            val termDate = if (termInfo.isSuccess) {
                termInfo.data!!.copy()
            } else {
                LogUtils.d<CourseManageViewModel>("Term Error! Term: ${termInfo.errorReason}")
                TimeUtils.generateNewTermDate()
            }

            val term: Term
            val courseData = if (courseInfo.isSuccess) {
                val styleList = CourseCellStyleDBHelper.loadCourseCellStyle(courseInfo.data!!)
                term = courseInfo.data.term.copy()
                ArrayList<Pair<Course, CourseCellStyle>>(courseInfo.data.courses.size).apply {
                    for (course in courseInfo.data.courses) {
                        add(Pair(course.copy(), CourseStyleUtils.getStyleByCourseId(course.id, styleList, copy = true)!!))
                    }
                    sortBy {
                        it.first.id
                    }
                }
            } else {
                LogUtils.d<CourseManageViewModel>("Course Data Error! Course: ${courseInfo.errorReason}")
                term = termDate.getTerm()
                ArrayList()
            }

            courseManagePkg.postValue(CourseManagePkg(termDate, term, courseData))
        }
    }

    fun refreshRawTermDate() {
        viewModelScope.launch(Dispatchers.Default) {
            val result = TermDateInfo.getInfo(TermDateInfo.TermType.RAW_TERM)
            if (result.isSuccess) {
                rawTermDate.postEventValue(result.data!!)
            } else {
                LogUtils.d<CourseManageViewModel>("Raw Term Data Refresh Failed! Reason: ${result.errorReason}")
            }
        }
    }

    fun importCourse(type: ImportCourseType) {
        viewModelScope.launch(Dispatchers.Default) {
            importLock.tryWithLock {
                val operationType = when (type) {
                    ImportCourseType.CURRENT_TERM -> CourseInfo.OperationType.THIS_TERM_COURSE
                    ImportCourseType.NEXT_TERM -> CourseInfo.OperationType.NEXT_TERM_COURSE
                }
                val asyncResult = CourseInfo.getInfo(operationType, forceRefresh = true)
                if (asyncResult.isSuccess) {
                    importCourseResult.postEventValue(Triple(asyncResult.data, type, null))
                    val conflictCheck = CourseSet.checkCourseTimeConflict(asyncResult.data!!.courses)
                    if (!conflictCheck.isSuccess) {
                        onlineCourseConflict.notifyEvent()
                        LogUtils.d<CourseManageViewModel>("Import Courses Has Conflicts!\n${conflictCheck.printText()}")
                    }
                } else {
                    importCourseResult.postEventValue(Triple(null, null, asyncResult.errorReason))
                }
            }
        }
    }

    fun saveAll(courseSet: CourseSet, styles: Array<CourseCellStyle>, termDate: TermDate) {
        viewModelScope.launch(Dispatchers.Default) {
            saveLock.tryWithLock {
                CourseInfo.saveNewCourses(courseSet)
                CourseCellStyleDBHelper.saveCourseCellStyle(styles)
                if (requireCleanTermDate) {
                    TermDateInfo.clearCustomTermDate()
                } else {
                    val result = TermDateInfo.getInfo(TermDateInfo.TermType.RAW_TERM, loadCache = true)
                    if (result.isSuccess) {
                        if (result.data!!.startDate == termDate.startDate && result.data.endDate == termDate.endDate) {
                            TermDateInfo.clearCustomTermDate()
                        } else {
                            TermDateInfo.saveCustomTermDate(termDate)
                        }
                    } else {
                        TermDateInfo.saveCustomTermDate(termDate)
                    }
                }
                saveSuccess.notifyEvent()
                NotifyBus[NotifyType.COURSE_STYLE_TERM_UPDATE].notifyEvent()
            }
        }
    }
}