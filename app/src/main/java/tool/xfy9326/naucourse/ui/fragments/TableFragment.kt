package tool.xfy9326.naucourse.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_table.*
import kotlinx.android.synthetic.main.fragment_table.view.*
import kotlinx.android.synthetic.main.view_course_table_header.*
import kotlinx.coroutines.*
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.ui.fragments.base.ViewModelFragment
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.helpers.CourseTableViewHelper
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.compute.TimeUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import kotlin.math.floor
import kotlin.properties.Delegates

class TableFragment : ViewModelFragment<CourseTableViewModel>() {
    private val tableScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val courseUpdateLock = Any()
    private var weekNum by Delegates.notNull<Int>()
    private var coursePkgHash = CourseTableViewModel.DEFAULT_COURSE_PKG_HASH
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

    override fun onCreateViewModel(): CourseTableViewModel = ViewModelProvider(requireParentFragment())[CourseTableViewModel::class.java]

    override fun prepareCacheInit(viewModel: CourseTableViewModel, isRestored: Boolean) {
        viewModel.apply {
            courseTablePkg[weekNum - 1].observe(viewLifecycleOwner, Observer {
                coursePkgHash = it.hashCode()
                buildCourseTableView(it)
            })
            courseAndTermEmpty.observeEvent(viewLifecycleOwner, Observer {
                showToast(requireContext(), R.string.course_and_term_data_empty)
            })
            if (!isRestored) {
                val temp = coursePkgSavedTemp[weekNum - 1]
                if (temp == null) {
                    requestCourseTable(weekNum, coursePkgHash)
                } else {
                    coursePkgHash = temp.hashCode()
                    buildCourseTableView(temp)
                }
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        // 由于ViewPager预加载，无法判断Fragment是否处于显示状态
        // 此处借助只有一个Fragment会控制Menu是否可见，来判断Fragment是否处于显示状态
        if (!menuVisible) {
            // 若当前课表不可见，则滚动回顶部
            requireView().layout_courseTable.apply {
                if (scrollY != 0) scrollTo(0, 0)
            }
        }
    }

    override fun bindViewModel(viewModel: CourseTableViewModel) {
        App.instance.requestRebuildCourseTable.observeNotification(viewLifecycleOwner, {
            val temp = viewModel.coursePkgSavedTemp[weekNum - 1]
            if (temp != null) buildCourseTableView(temp)
        }, "${TableFragment::class.java.simpleName}-$weekNum")
    }

    override fun onDestroy() {
        super.onDestroy()
        tableScope.cancel()
    }

    private fun buildCourseTableView(coursePkg: CoursePkg) {
        tableScope.launch {
            synchronized(courseUpdateLock) {
                launch {
                    val dateInfo = async { TimeUtils.getWeekNumDateArray(coursePkg.termDate.startDate, weekNum) }
                    val today = async { TimeUtils.getTodayDate() }
                    val hasWeekendCourse = SettingsPref.ForceShowCourseTableWeekends || CourseTableViewHelper.hasWeekendCourse(coursePkg.courseTable)
                    val weekDayShowSize = getWeekDayShowSize(hasWeekendCourse)
                    buildCourseTableHeader(today.await(), dateInfo.await(), weekDayShowSize, hasWeekendCourse)

                    val width = calculateTableHeaderWidth(weekDayShowSize)
                    CourseTableViewHelper.createCourseTableView(
                        requireContext(),
                        hasWeekendCourse,
                        coursePkg,
                        gl_courseTable,
                        width,
                        CourseTableViewHelper.CourseTableStyle(
                            SettingsPref.SameCourseCellHeight,
                            SettingsPref.CourseTableRoundCompat,
                            SettingsPref.CenterHorizontalShowCourseText
                        )
                    )
                }
            }
        }
    }

    private fun getWeekDayShowSize(hasWeekendCourse: Boolean) =
        if (hasWeekendCourse) {
            CourseTableViewHelper.DEFAULT_TABLE_WIDTH_SIZE - 1
        } else {
            CourseTableViewHelper.DEFAULT_TABLE_WIDTH_SIZE - 3
        }

    private fun calculateTableHeaderWidth(weekDayShowSize: Int): Pair<Int, Int> {
        val timeRowWidth = resources.getDimensionPixelSize(R.dimen.course_table_course_time_row_size)
        return Pair(timeRowWidth, floor((CourseTableViewHelper.getWindowsWidth(requireContext()) - timeRowWidth) * 1f / weekDayShowSize).toInt())
    }

    private suspend fun buildCourseTableHeader(
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
                CourseTableViewHelper.setDateCellView(i + 1, dateInfo.second[i], courseTableHeaderDate[i], isToday)
            }
        }
}