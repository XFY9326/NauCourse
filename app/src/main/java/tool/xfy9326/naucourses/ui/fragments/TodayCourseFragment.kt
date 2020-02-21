package tool.xfy9326.naucourses.ui.fragments

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.fragment.TodayCourseViewModel

class TodayCourseFragment(activity: AppCompatActivity, idRes: Int) :
    DrawerToolbarFragment<TodayCourseViewModel>(activity, idRes) {
    override fun onCreateContentView(): Int = R.layout.fragment_today_course

    override fun onCreateViewModel(): TodayCourseViewModel = ViewModelProvider(this)[TodayCourseViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_general

    override fun initView(viewModel: TodayCourseViewModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}