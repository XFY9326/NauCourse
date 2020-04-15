package tool.xfy9326.naucourse.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.view_course_table.*
import kotlinx.android.synthetic.main.view_course_table_header.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.table.CourseTableViewHelper
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.BaseUtils.tryWithLock
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import tool.xfy9326.naucourse.utils.views.ViewUtils.runInMain
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

        if (savedInstanceState == null) {
            contentViewModel.requestCourseTable(weekNum, coursePkgHash)
        }
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
                courseTablePkg[weekNum - 1].value?.let {
                    startBuildTable(it)
                }
            }, "${TableFragment::class.java.simpleName}-$weekNum")
        }
    }

    private fun startBuildTable(coursePkg: CoursePkg) {
        lifecycleScope.launch(Dispatchers.Default) {
            buildCourseTableView(coursePkg)
            coursePkgHash = coursePkg.hashCode()
        }
    }

    private suspend fun buildCourseTableView(coursePkg: CoursePkg) =
        withContext(Dispatchers.Default) {
            courseUpdateLock.tryWithLock {
                val columnSize = coursePkg.showWeekDaySize + 1

                val table = async {
                    CourseTableViewHelper.buildCourseTable(requireContext(), coursePkg, resources.displayMetrics.widthPixels, columnSize)
                }

                val dateInfo = TimeUtils.getWeekNumDateArray(coursePkg.termDate, weekNum)

                val headerAsync = if (layout_courseTableHeader != null) {
                    async {
                        CourseTableViewHelper.buildCourseTableHeader(
                            requireContext(),
                            coursePkg.showWeekDaySize,
                            dateInfo,
                            layout_courseTableHeader,
                            coursePkg.courseTableStyle
                        )
                    }
                } else {
                    null
                }

                gl_courseTable?.runInMain {
                    CourseTableViewHelper.applyViewToCourseTable(it, table.await(), columnSize, coursePkg.courseTableStyle)
                }

                headerAsync?.await()?.let { views ->
                    layout_courseTableHeader.runInMain {
                        CourseTableViewHelper.applyViewToCourseTableHeader(
                            requireContext(), it,
                            views, dateInfo, coursePkg.courseTableStyle
                        )
                    }
                }
            }
        }
}