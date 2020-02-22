package tool.xfy9326.naucourses.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_table.*
import kotlinx.android.synthetic.main.view_course_table_header.*
import kotlinx.coroutines.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CoursePkg
import tool.xfy9326.naucourses.ui.fragments.base.ViewModelFragment
import tool.xfy9326.naucourses.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourses.ui.views.table.CourseTableViewBuilder
import tool.xfy9326.naucourses.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourses.utils.compute.TimeUtils
import kotlin.math.floor
import kotlin.properties.Delegates

class TableFragment : ViewModelFragment<CourseTableViewModel>() {
    private val courseTableScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var weekNum by Delegates.notNull<Int>()
    private var coursePkgHash = 0
    private val courseTableHeaderDate by lazy {
        arrayOf(
            layout_cellDateMon,
            layout_cellDateTue,
            layout_cellDateWed,
            layout_cellDateThu,
            layout_cellDateFri,
            layout_cellDateSat,
            layout_cellDateSun
        )
    }

    override fun onCreateContentView(): Int = R.layout.fragment_table

    override fun onSetInstance(args: Bundle?) {
        weekNum = args?.getInt(CourseTableViewPagerAdapter.COURSE_TABLE_WEEK_NUM)!!
    }

    override fun onCreateViewModel(savedInstanceState: Bundle?): CourseTableViewModel =
        ViewModelProvider(activity!!)[CourseTableViewModel::class.java].apply {
            courseTablePkg[weekNum - 1].observe(this@TableFragment, Observer {
                coursePkgHash = it.hashCode()
                coursePkgSavedTemp[weekNum - 1] = it
                updateCourseTableView(it)
            })
            if (savedInstanceState == null) {
                if (coursePkgSavedTemp[weekNum - 1] == null) {
                    requestCourseTable(weekNum, coursePkgHash)
                } else {
                    coursePkgHash = coursePkgSavedTemp[weekNum - 1]!!.hashCode()
                    updateCourseTableView(coursePkgSavedTemp[weekNum - 1]!!)
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        courseTableScope.cancel()
    }

    private fun updateCourseTableView(coursePkg: CoursePkg) {
        courseTableScope.launch {
            val dateInfo = async { TimeUtils.getWeekNumDateArray(coursePkg.termDate.startDate, weekNum) }
            val today = async { TimeUtils.getTodayDate() }
            val hasWeekendCourse = CourseTableViewBuilder.hasWeekendCourse(coursePkg.courseTable)
            val weekDayShowSize = getWeekDayShowSize(hasWeekendCourse)
            updateCourseTableHeader(today.await(), dateInfo.await(), weekDayShowSize, hasWeekendCourse)

            val width = calculateTableHeaderWidth(weekDayShowSize)
            CourseTableViewBuilder.createCourseTableView(
                context!!,
                coursePkg.courseTable,
                hasWeekendCourse,
                coursePkg.styles,
                gl_courseTable,
                width
            )
        }
    }

    private fun getWeekDayShowSize(hasWeekendCourse: Boolean) =
        if (hasWeekendCourse) {
            CourseTableViewBuilder.DEFAULT_TABLE_WIDTH_SIZE - 1
        } else {
            CourseTableViewBuilder.DEFAULT_TABLE_WIDTH_SIZE - 3
        }

    private fun calculateTableHeaderWidth(weekDayShowSize: Int): Pair<Int, Int> {
        val timeRowWidth = resources.getDimensionPixelSize(R.dimen.course_table_course_time_row_size)
        return Pair(timeRowWidth, floor((CourseTableViewBuilder.getWindowsWidth(context!!) - timeRowWidth) * 1f / weekDayShowSize).toInt())
    }

    private suspend fun updateCourseTableHeader(
        today: Pair<Int, Int>, dateInfo: Pair<Int, Array<Int>>, weekDayShowSize: Int,
        hasWeekendCourse: Boolean
    ) =
        withContext(Dispatchers.Main) {
            tv_cellMonth.text = getString(R.string.month, dateInfo.first)

            if (hasWeekendCourse) {
                courseTableHeaderDate[courseTableHeaderDate.size - 1].visibility = View.VISIBLE
                courseTableHeaderDate[courseTableHeaderDate.size - 2].visibility = View.VISIBLE
            } else {
                courseTableHeaderDate[courseTableHeaderDate.size - 1].visibility = View.GONE
                courseTableHeaderDate[courseTableHeaderDate.size - 2].visibility = View.GONE
            }
            for (i in 0 until weekDayShowSize) {
                val isToday = dateInfo.first == today.first && dateInfo.second[i] == today.second
                CourseTableViewBuilder.setDateCellView(i + 1, dateInfo.second[i], courseTableHeaderDate[i], isToday)
            }
        }
}