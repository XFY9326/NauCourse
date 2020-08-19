package tool.xfy9326.naucourse.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_table.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCell
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.kt.runInMain
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.table.CourseTableHeaderView
import tool.xfy9326.naucourse.ui.views.table.CourseTableView
import tool.xfy9326.naucourse.ui.views.table.CourseTableViewHelper
import tool.xfy9326.naucourse.ui.views.table.OnCourseCellClickListener
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import kotlin.properties.Delegates

class TableFragment : Fragment(), Observer<CoursePkg>, OnCourseCellClickListener {
    private lateinit var contentViewModel: CourseTableViewModel

    private val courseUpdateLock = Mutex()
    private var weekNum by Delegates.notNull<Int>()

    override fun onAttach(context: Context) {
        weekNum = arguments?.getInt(CourseTableViewPagerAdapter.COURSE_TABLE_WEEK_NUM)!!
        contentViewModel = ViewModelProvider(requireParentFragment())[CourseTableViewModel::class.java]

        contentViewModel.courseTablePkg[weekNum - 1].observeForever(this)
        if (contentViewModel.tryInitTable(weekNum)) {
            contentViewModel.requestCourseTable(weekNum)
        }

        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (view == null) {
            inflater.inflate(R.layout.fragment_table, container, false)
        } else {
            val parent = requireView().parent as ViewGroup?
            parent?.removeView(view)
            view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        applyLayoutTransition(layout_emptyCourseTableHeader)
        applyLayoutTransition(layout_emptyCourseTable)

        contentViewModel.courseAndTermEmpty.observeNotification(viewLifecycleOwner, "${TableFragment::class.java.simpleName}-$weekNum") {
            showShortToast(R.string.course_and_term_data_empty)
        }
    }

    override fun onChanged(t: CoursePkg) {
        lifecycleScope.launch {
            courseUpdateLock.withLock {
                buildCourseTableView(t)
            }
        }
    }

    private suspend fun buildCourseTableView(coursePkg: CoursePkg) = withContext(Dispatchers.Default) {
        val showWeekDaySize = CourseTableViewHelper.getShowWeekDaySize(coursePkg.courseTable, coursePkg.courseTableStyle)
        val dateInfo = TimeUtils.getWeekNumDateArray(coursePkg.termDate, weekNum)

        if (view != null) {
            layout_emptyCourseTable.apply {
                if (childCount > 0) {
                    (getChildAt(0) as CourseTableView).setTableData(coursePkg, showWeekDaySize + 1)
                } else {
                    val courseTable = CourseTableView.create(requireContext(), coursePkg, showWeekDaySize + 1).apply {
                        setOnCourseCellClickListener(this@TableFragment)
                    }
                    runInMain {
                        it.addView(courseTable)
                    }
                }
            }
            layout_emptyCourseTableHeader.apply {
                if (childCount > 0) {
                    runInMain {
                        (it.getChildAt(0) as CourseTableHeaderView).setHeaderData(showWeekDaySize, dateInfo, coursePkg.courseTableStyle)
                    }
                } else {
                    val courseTableHeader =
                        CourseTableHeaderView.create(requireContext(), showWeekDaySize, dateInfo, coursePkg.courseTableStyle)
                    runInMain {
                        it.addView(courseTableHeader)
                    }
                }
            }
        } else {
            val courseTable = CourseTableView.create(requireContext(), coursePkg, showWeekDaySize + 1).apply {
                setOnCourseCellClickListener(this@TableFragment)
            }
            lifecycleScope.launchWhenStarted {
                layout_emptyCourseTable.addView(courseTable)
            }

            val courseTableHeader = CourseTableHeaderView.create(requireContext(), showWeekDaySize, dateInfo, coursePkg.courseTableStyle)
            lifecycleScope.launchWhenStarted {
                layout_emptyCourseTableHeader.addView(courseTableHeader)
            }
        }
        return@withContext
    }

    private fun applyLayoutTransition(container: ViewGroup) {
        container.layoutTransition?.apply {
            setAnimateParentHierarchy(false)
            setDuration(150)
        }
    }

    override fun onCourseCellClick(courseCell: CourseCell, cellStyle: CourseCellStyle) {
        contentViewModel.requestCourseDetailInfo(courseCell, cellStyle)
    }

    override fun onDetach() {
        contentViewModel.courseTablePkg[weekNum - 1].removeObserver(this)
        super.onDetach()
    }
}