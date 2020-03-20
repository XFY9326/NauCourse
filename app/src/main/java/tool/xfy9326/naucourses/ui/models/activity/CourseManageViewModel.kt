package tool.xfy9326.naucourses.ui.models.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseManagePkg
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.tools.EventLiveData
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import tool.xfy9326.naucourses.utils.utility.LogUtils

class CourseManageViewModel : BaseViewModel() {
    val rawTermDate = EventLiveData<TermDate>()
    val courseManagePkg = MutableLiveData<CourseManagePkg>()
    val saveSuccess = EventLiveData(false)

    var colorEditPosition: Int? = null
    var colorEditStyle: CourseCellStyle? = null

    var requireCleanTermDate: Boolean = false

    private val saveLock = Mutex()

    override fun onInitView(isRestored: Boolean) {
        requestCourseManagePkg()
    }

    private fun requestCourseManagePkg() {
        viewModelScope.launch(Dispatchers.Default) {
            val courseInfoAsync = async { CourseInfo.getInfo(loadCache = true) }
            val termInfoAsync = async { TermDateInfo.getInfo(loadCache = true) }

            val termInfo = termInfoAsync.await()
            val courseInfo = courseInfoAsync.await()

            if (termInfo.isSuccess && courseInfo.isSuccess) {
                val styleList = CourseCellStyleStore.loadCellStyles(courseInfo.data!!)
                val output = ArrayList<Pair<Course, CourseCellStyle>>(courseInfo.data.courses.size).apply {
                    for (course in courseInfo.data.courses) {
                        add(Pair(course.copy(), CourseCellStyle.getStyleByCourseId(course.id, styleList, copy = true)!!))
                    }
                    sortBy {
                        it.first.id
                    }
                }
                courseManagePkg.postValue(CourseManagePkg(termInfo.data!!.copy(), courseInfo.data.term.copy(), output))
            } else {
                LogUtils.d<CourseManageViewModel>("Term Or Course Data Error! Term: ${termInfo.errorReason}  Course: ${courseInfo.errorReason}")
            }
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

    fun saveAll(courseSet: CourseSet, styles: Array<CourseCellStyle>, termDate: TermDate) {
        viewModelScope.launch(Dispatchers.Default) {
            saveLock.withLock {
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
                App.instance.courseTermUpdate.postEventValue(true)
            }
        }
    }
}