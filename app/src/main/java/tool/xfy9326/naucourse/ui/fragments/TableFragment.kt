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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.table.CourseTableViewHelper
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import kotlin.properties.Delegates

class TableFragment : Fragment(), Observer<CoursePkg> {
    private lateinit var contentViewModel: CourseTableViewModel

    private val courseUpdateLock = Mutex()
    private var weekNum by Delegates.notNull<Int>()

    override fun onAttach(context: Context) {
        weekNum = arguments?.getInt(CourseTableViewPagerAdapter.COURSE_TABLE_WEEK_NUM)!!
        contentViewModel = ViewModelProvider(requireParentFragment())[CourseTableViewModel::class.java]

        initTableView()
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

        contentViewModel.courseAndTermEmpty.observeNotification(viewLifecycleOwner, {
            showToast(requireContext(), R.string.course_and_term_data_empty)
        }, "${TableFragment::class.java.simpleName}-$weekNum")
    }

    override fun onChanged(t: CoursePkg) {
        lifecycleScope.launch(Dispatchers.Default) {
            buildCourseTableView(t)
        }
    }

    private fun initTableView() {
        contentViewModel.courseTablePkg[weekNum - 1].observeForever(this)
        if (contentViewModel.tryInitTable(weekNum)) {
            contentViewModel.requestCourseTable(weekNum)
        }
    }

    private suspend fun buildCourseTableView(coursePkg: CoursePkg) = coroutineScope {
        courseUpdateLock.withLock {
            val showWeekDaySize = CourseTableViewHelper.getShowWeekDaySize(coursePkg.courseTable, coursePkg.courseTableStyle)

            val courseTableAsync = async {
                CourseTableViewHelper.buildCourseTable(
                    requireContext(),
                    coursePkg,
                    resources.displayMetrics.widthPixels,
                    showWeekDaySize + 1
                )
            }

            val courseTableHeaderAsync = async {
                CourseTableViewHelper.buildCourseTableHeader(
                    requireContext(),
                    showWeekDaySize,
                    TimeUtils.getWeekNumDateArray(coursePkg.termDate, weekNum),
                    coursePkg.courseTableStyle
                )
            }

            val courseTableHeader = courseTableHeaderAsync.await()
            val courseTable = courseTableAsync.await()

            lifecycleScope.launchWhenStarted {
                replaceAllView(layout_emptyCourseTable, courseTable)
                replaceAllView(layout_emptyCourseTableHeader, courseTableHeader)
            }
        }
    }

    private fun applyLayoutTransition(container: ViewGroup) {
        container.layoutTransition?.apply {
            setAnimateParentHierarchy(false)
            setDuration(150)
        }
    }

    private fun replaceAllView(container: ViewGroup, view: View) {
        container.apply {
            if (childCount > 0) removeAllViews()
            addView(view)
        }
    }

    override fun onDetach() {
        contentViewModel.courseTablePkg[weekNum - 1].removeObserver(this)
        super.onDetach()
    }
}