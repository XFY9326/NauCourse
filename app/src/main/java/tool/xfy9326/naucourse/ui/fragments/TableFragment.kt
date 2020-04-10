package tool.xfy9326.naucourse.ui.fragments

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.table.CourseTableStyle
import tool.xfy9326.naucourse.ui.views.table.CourseTableViewHelper
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import kotlin.properties.Delegates

class TableFragment : Fragment() {
    private lateinit var contentViewModel: CourseTableViewModel

    private val courseUpdateLock = Mutex()
    private var weekNum by Delegates.notNull<Int>()
    private var coursePkgHash = CourseTableViewModel.DEFAULT_COURSE_PKG_HASH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weekNum = arguments?.getInt(CourseTableViewPagerAdapter.COURSE_TABLE_WEEK_NUM)!!
        contentViewModel = ViewModelProvider(requireParentFragment())[CourseTableViewModel::class.java]
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
        gl_courseTable?.layoutTransition?.setAnimateParentHierarchy(false)

        bindObserver()
        if (savedInstanceState == null) {
            prepareCourseTable()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun bindObserver() {
        contentViewModel.apply {
            courseTablePkg[weekNum - 1].observe(viewLifecycleOwner, Observer {
                startBuildTable(it)
            })

            courseAndTermEmpty.observeNotification(viewLifecycleOwner, {
                showToast(requireContext(), R.string.course_and_term_data_empty)
            }, "${TableFragment::class.java.simpleName}-$weekNum")

            courseTableRebuild.observeNotification(viewLifecycleOwner, {
                val temp = coursePkgSavedTemp[weekNum - 1]
                if (temp != null) {
                    lifecycleScope.launch(Dispatchers.Default) {
                        buildCourseTableView(temp, getCourseTableStyle())
                    }
                }
            }, "${TableFragment::class.java.simpleName}-$weekNum")
        }
    }

    private fun prepareCourseTable() {
        contentViewModel.apply {
            val temp = coursePkgSavedTemp[weekNum - 1]
            if (temp == null) {
                requestCourseTable(weekNum, coursePkgHash)
            } else {
                startBuildTable(temp)
            }
        }
    }

    private fun startBuildTable(coursePkg: CoursePkg) {
        lifecycleScope.launch(Dispatchers.Default) {
            buildCourseTableView(coursePkg, contentViewModel.getCourseTableStyle())
            coursePkgHash = coursePkg.hashCode()
        }
    }

    private suspend fun buildCourseTableView(coursePkg: CoursePkg, courseTableStyle: CourseTableStyle) =
        withContext(Dispatchers.Default) {
            if (courseUpdateLock.tryLock()) {
                try {
                    val showWeekend = courseTableStyle.forceShowCourseTableWeekends || CourseUtils.hasWeekendCourse(coursePkg.courseTable)
                    val showWeekDaySize = CourseTableViewHelper.getShowWeekDaySize(showWeekend)
                    val columnSize = showWeekDaySize + 1

                    val table = async {
                        CourseTableViewHelper.buildCourseTable(
                            requireContext(), coursePkg,
                            requireContext().resources.displayMetrics.widthPixels, columnSize, courseTableStyle
                        )
                    }
                    val header = if (layout_courseTableHeader != null) {
                        async {
                            CourseTableViewHelper.buildCourseTableHeader(
                                requireContext(),
                                coursePkg.termDate,
                                weekNum,
                                showWeekDaySize,
                                layout_courseTableHeader,
                                courseTableStyle
                            )
                        }
                    } else {
                        null
                    }

                    if (gl_courseTable != null) {
                        CourseTableViewHelper.applyViewToCourseTable(gl_courseTable, table.await(), columnSize, courseTableStyle)
                    }

                    header?.await()
                } finally {
                    courseUpdateLock.unlock()
                }
            }
        }
}