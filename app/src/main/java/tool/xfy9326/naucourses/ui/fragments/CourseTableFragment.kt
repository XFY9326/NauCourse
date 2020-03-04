package tool.xfy9326.naucourses.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_course_table.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseCell
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.io.prefs.CourseTablePref
import tool.xfy9326.naucourses.ui.dialogs.CourseDetailDialog
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourses.ui.views.helpers.CourseTableViewHelper
import tool.xfy9326.naucourses.ui.views.viewpager.CourseTableViewPagerAdapter

class CourseTableFragment : DrawerToolbarFragment<CourseTableViewModel>(), CourseTableViewHelper.OnCourseCellClickListener {
    private lateinit var courseTableViewPagerAdapter: CourseTableViewPagerAdapter
    private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateContentView(): Int = R.layout.fragment_course_table

    override fun onCreateViewModel(): CourseTableViewModel = ViewModelProvider(this)[CourseTableViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_courseTable

    override fun bindViewModel(viewModel: CourseTableViewModel) {
        viewModel.nowShowWeekNum.observe(viewLifecycleOwner, Observer {
            tv_nowShowWeekNum.text = getString(R.string.week_num, it)
            viewModel.requestShowWeekStatus(it)
        })
        viewModel.currentWeekStatus.observe(viewLifecycleOwner, Observer {
            when (it!!) {
                CourseTableViewModel.CurrentWeekStatus.IN_VACATION -> {
                    tv_notCurrentWeek.visibility = View.VISIBLE
                    tv_notCurrentWeek.setText(R.string.in_vacation)
                }
                CourseTableViewModel.CurrentWeekStatus.IS_CURRENT_WEEK -> {
                    tv_notCurrentWeek.visibility = View.GONE
                    tv_notCurrentWeek.text = Constants.EMPTY
                }
                CourseTableViewModel.CurrentWeekStatus.NOT_CURRENT_WEEK -> {
                    tv_notCurrentWeek.visibility = View.VISIBLE
                    tv_notCurrentWeek.setText(R.string.not_current_week)
                }
            }
        })
        viewModel.todayDate.observe(viewLifecycleOwner, Observer {
            tv_todayDate.text = getString(R.string.today_date, it.first, it.second)
        })
        viewModel.maxWeekNum.observe(viewLifecycleOwner, Observer {
            courseTableViewPagerAdapter.updateMaxWeekNum(it)
            viewModel.maxWeekNumTemp = it
        })
        viewModel.nowWeekNum.observe(viewLifecycleOwner, Observer {
            synchronized(this) {
                if (!viewModel.hasInitWithNowWeekNum) {
                    viewModel.hasInitWithNowWeekNum = true
                    val weekNum = if (it == 0) 1 else it
                    viewModel.nowShowWeekNum.postValue(weekNum - 1)
                    vp_courseTablePanel.setCurrentItem(weekNum - 1, false)
                }
            }
        })
        viewModel.courseDetailInfo.observeEvent(viewLifecycleOwner, Observer {
            CourseDetailDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(CourseDetailDialog.COURSE_DETAIL_DATA, it)
                }
            }.show(childFragmentManager, null)
        })
    }

    override fun initView(viewModel: CourseTableViewModel) {
        CourseTableViewHelper.initBuilder(requireActivity(), this)
        courseTableViewPagerAdapter = CourseTableViewPagerAdapter(this, viewModel.maxWeekNumTemp ?: Constants.Course.MAX_WEEK_NUM_SIZE)

        vp_courseTablePanel.offscreenPageLimit = 2
        vp_courseTablePanel.adapter = courseTableViewPagerAdapter

        setToolbarTitleEnabled(false)

        viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.nowShowWeekNum.postValue(position + 1)
            }
        }
        vp_courseTablePanel.registerOnPageChangeCallback(viewPagerCallback)

        v_courseTableCornerCompat.visibility = if (CourseTablePref.RoundCornerDeviceCompat) {
            View.VISIBLE
        } else {
            View.GONE
        }

        layout_dateInfoBar.setOnClickListener {
            if (viewModel.currentWeekNum != null) {
                vp_courseTablePanel.setCurrentItem(viewModel.currentWeekNum!! - 1, true)
            }
        }
    }

    override fun onCourseCellClick(courseCell: CourseCell, cellStyle: CourseCellStyle) {
        getViewModel().requestCourseDetailInfo(courseCell, cellStyle)
    }

    override fun onDestroyView() {
        vp_courseTablePanel.unregisterOnPageChangeCallback(viewPagerCallback)
        super.onDestroyView()
    }
}