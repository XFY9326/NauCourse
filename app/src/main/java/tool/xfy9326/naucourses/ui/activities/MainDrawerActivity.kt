package tool.xfy9326.naucourses.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_nav_header.view.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourses.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourses.ui.fragments.CourseTablePanelFragment
import tool.xfy9326.naucourses.ui.fragments.NewsFragment
import tool.xfy9326.naucourses.ui.fragments.TodayCourseFragment
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.activity.MainDrawerViewModel

class MainDrawerActivity : ViewModelActivity<MainDrawerViewModel>(), NavigationView.OnNavigationItemSelectedListener {
    @Volatile
    private var nowFragmentType: FragmentType = DEFAULT_FRAGMENT

    companion object {
        private val DEFAULT_FRAGMENT = FragmentType.COURSE_TABLE

        enum class FragmentType {
            COURSE_TABLE,
            TODAY_COURSE,
            NEWS
        }
    }

    private val fragmentMap = mapOf<FragmentType, Lazy<DrawerToolbarFragment<*>>>(
        FragmentType.COURSE_TABLE to lazy { CourseTablePanelFragment(this, R.id.drawer_main) },
        FragmentType.TODAY_COURSE to lazy { TodayCourseFragment(this, R.id.drawer_main) },
        FragmentType.NEWS to lazy { NewsFragment(this, R.id.drawer_main) }
    )

    override fun onCreateContentView(): Int = R.layout.activity_main

    override fun onCreateViewModel(): MainDrawerViewModel = ViewModelProvider(this)[MainDrawerViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: MainDrawerViewModel) {
        nav_main.setNavigationItemSelectedListener(this)
        nav_main.getHeaderView(0).setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }
        showFragment(DEFAULT_FRAGMENT)
    }

    @Synchronized
    private fun showFragment(type: FragmentType) {
        val fragment = (fragmentMap[type] ?: error("Fragment Not Found!!")).value
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fg_mainContent, fragment)
        }.commit()
        nowFragmentType = type
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_navTodayCourse -> showFragment(FragmentType.TODAY_COURSE)
            R.id.menu_navCourseTable -> showFragment(FragmentType.COURSE_TABLE)
            R.id.menu_navNews -> showFragment(FragmentType.NEWS)
            R.id.menu_navExit -> finishAndRemoveTask()
        }
        drawer_main.closeDrawers()
        return true
    }

    override fun bindViewModel(viewModel: MainDrawerViewModel) {
        viewModel.studentCardBalance.observe(this, Observer {
            nav_main.getHeaderView(0).tv_cardBalance.text = getString(R.string.balance, String.format(Constants.KEEP_TWO_DECIMAL_PLACES, it))
        })
        viewModel.studentInfo.observe(this, Observer {
            nav_main.getHeaderView(0).tv_userId.text = it.personalInfo.stuId.second
            nav_main.getHeaderView(0).tv_userName.text = StudentInfo.trimExtra(it.personalInfo.name.second)
        })
        drawer_main.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerOpened(drawerView: View) = viewModel.updateBalance()
        })
    }

    override fun onBackPressed() {
        if (drawer_main.isDrawerOpen(GravityCompat.START)) {
            drawer_main.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(false)
        }
    }
}