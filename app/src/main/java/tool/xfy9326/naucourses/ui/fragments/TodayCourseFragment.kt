package tool.xfy9326.naucourses.ui.fragments

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.fragment.TodayCourseViewModel

class TodayCourseFragment : DrawerToolbarFragment<TodayCourseViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateContentView(): Int = R.layout.fragment_today_course

    override fun onCreateViewModel(): TodayCourseViewModel = ViewModelProvider(this)[TodayCourseViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_general

    override fun initView(viewModel: TodayCourseViewModel) {

    }
}