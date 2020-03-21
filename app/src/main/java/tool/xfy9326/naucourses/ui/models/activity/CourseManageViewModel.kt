package tool.xfy9326.naucourses.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseManagePkg
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.Term
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.tools.EventLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.debug.LogUtils

class CourseManageViewModel : BaseViewModel() {
    val rawTermDate = EventLiveData<TermDate>()
    val importCourseResult = EventLiveData<Pair<CourseSet, ImportCourseType>?>()
    val courseManagePkg = MutableLiveData<CourseManagePkg>()
    val saveSuccess = EventLiveData(false)

    var colorEditPosition: Int? = null
    var colorEditStyle: CourseCellStyle? = null

    var requireCleanTermDate: Boolean = false

    private val saveLock = Mutex()
    private val importLock = Mutex()

    override fun onInitView(isRestored: Boolean) {
        requestCourseManagePkg()
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
                TermDate.generateNewTermDate()
            }

            val term: Term
            val courseData = if (courseInfo.isSuccess) {
                val styleList = CourseCellStyleStore.loadCellStyles(courseInfo.data!!)
                term = courseInfo.data.term.copy()
                ArrayList<Pair<Course, CourseCellStyle>>(courseInfo.data.courses.size).apply {
                    for (course in courseInfo.data.courses) {
                        add(Pair(course.copy(), CourseCellStyle.getStyleByCourseId(course.id, styleList, copy = true)!!))
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
            val result = TermDateInfo.getInfo(TermDateInfo.TermType.RAW_TERM, loadCache = true)
            if (result.isSuccess) {
                rawTermDate.postEventValue(result.data!!)
            } else {
                LogUtils.d<CourseManageViewModel>("Raw Term Data Refresh Failed! Reason: ${result.errorReason}")
            }
        }
    }

    fun importCourse(type: ImportCourseType) {
        viewModelScope.launch(Dispatchers.Default) {
            if (importLock.tryLock()) {
                try {
                    val operationType = when (type) {
                        ImportCourseType.CURRENT_TERM -> CourseInfo.OperationType.THIS_TERM_COURSE
                        ImportCourseType.NEXT_TERM -> CourseInfo.OperationType.NEXT_TERM_COURSE
                    }
                    val asyncResult = CourseInfo.getInfo(operationType, forceRefresh = true)
                    if (asyncResult.isSuccess) {
                        importCourseResult.postEventValue(Pair(asyncResult.data!!, type))
                    } else {
                        importCourseResult.postEventValue(null)
                        LogUtils.d<CourseManageViewModel>("Course Import Failed! Type: $type  Reason: ${asyncResult.errorReason}")
                    }
                } finally {
                    importLock.unlock()
                }
            }
        }
    }

    fun saveAll(courseSet: CourseSet, styles: Array<CourseCellStyle>, termDate: TermDate) {
        viewModelScope.launch(Dispatchers.Default) {
            if (saveLock.tryLock()) {
                try {
                    CourseInfo.saveNewCourses(courseSet)
                    CourseCellStyleStore.saveStore(styles)
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
                    saveSuccess.postEventValue(true)
                    App.instance.courseStyleTermUpdate.postEventValue(true)
                } finally {
                    saveLock.unlock()
                }
            }
        }
    }
}