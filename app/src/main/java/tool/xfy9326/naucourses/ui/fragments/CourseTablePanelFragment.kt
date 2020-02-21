package tool.xfy9326.naucourses.ui.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_course_table_panel.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourses.ui.views.builder.CourseTableViewBuilder
import tool.xfy9326.naucourses.ui.views.viewpager.CourseTableViewPagerAdapter
import kotlin.properties.Delegates

class CourseTablePanelFragment(activity: AppCompatActivity, idRes: Int) : DrawerToolbarFragment<CourseTableViewModel>(activity, idRes) {
    private var nowShowWeekNum by Delegates.notNull<Int>()

    @Volatile
    private lateinit var courseTableViewPagerAdapter: CourseTableViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateContentView(): Int = R.layout.fragment_course_table_panel

    override fun onCreateViewModel(): CourseTableViewModel = ViewModelProvider(this)[CourseTableViewModel::class.java].apply { initData() }

    override fun onBindToolbar(): Toolbar = tb_courseTable

    override fun bindViewModel(viewModel: CourseTableViewModel) {
        viewModel.maxWeekNum.observe(this, Observer {
            courseTableViewPagerAdapter.updateMaxWeekNum(it)
        })
        viewModel.nowWeekNum.observe(this, Observer {
            vp_courseTablePanel.currentItem = it - 1
            nowShowWeekNum = it
        })
    }

    override fun initView(viewModel: CourseTableViewModel) {
        CourseTableViewBuilder.initBuilder(activity!!)
        courseTableViewPagerAdapter = CourseTableViewPagerAdapter(this, Constants.Course.MAX_WEEK_NUM_SIZE)
        vp_courseTablePanel.adapter = courseTableViewPagerAdapter
        vp_courseTablePanel.offscreenPageLimit = 2
        vp_courseTablePanel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                nowShowWeekNum = position + 1
            }
        })
    }
}