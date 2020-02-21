package tool.xfy9326.naucourses.ui.fragments

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.fragment.NewsViewModel

class NewsFragment(activity: AppCompatActivity, idRes: Int) :
    DrawerToolbarFragment<NewsViewModel>(activity, idRes) {
    override fun onCreateContentView(): Int = R.layout.fragment_news

    override fun onCreateViewModel(): NewsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_general

    override fun initView(viewModel: NewsViewModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}