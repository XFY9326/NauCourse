package tool.xfy9326.naucourses.ui.activities

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_course_manage.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourses.ui.models.activity.CourseManageViewModel
import tool.xfy9326.naucourses.ui.views.recyclerview.AdvancedDivider
import tool.xfy9326.naucourses.ui.views.recyclerview.adapters.CourseAdapter
import tool.xfy9326.naucourses.utils.BaseUtils.dpToPx
import tool.xfy9326.naucourses.utils.views.ActivityUtils.enableHomeButton


class CourseManageActivity : ViewModelActivity<CourseManageViewModel>() {
    private val dividerLeftMargin = 50.dpToPx()
    private val dividerRightMargin = 10.dpToPx()

    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateContentView(): Int = R.layout.activity_course_manage

    override fun onCreateViewModel(): CourseManageViewModel = ViewModelProvider(this)[CourseManageViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: CourseManageViewModel) {
        setSupportActionBar(tb_general)
        enableHomeButton()

        courseAdapter = CourseAdapter(this)

        arv_courseManageList.apply {
            addItemDecoration(
                AdvancedDivider(
                    this@CourseManageActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setMargins(dividerLeftMargin, dividerRightMargin)
                })
            adapter = courseAdapter
        }
    }

    override fun bindViewModel(viewModel: CourseManageViewModel) {
        viewModel.courseManagePkg.observe(this, Observer {
            courseAdapter.setCourseManagePkg(it)
        })
    }
}