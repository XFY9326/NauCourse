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
import tool.xfy9326.naucourses.ui.fragments.CourseTableFragment
import tool.xfy9326.naucourses.ui.fragments.NewsFragment
import tool.xfy9326.naucourses.ui.fragments.TodayCourseFragment
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.activity.MainDrawerViewModel

class MainDrawerActivity : ViewModelActivity<MainDrawerViewModel>(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val DEFAULT_NAV_HEADER_INDEX = 0

        enum class FragmentType {
            COURSE_TABLE,
            TODAY_COURSE,
            NEWS,
            NONE
        }

        private val FRAGMENTS = mapOf<FragmentType, DrawerToolbarFragment<*>>(
            FragmentType.COURSE_TABLE to CourseTableFragment(),
            FragmentType.TODAY_COURSE to TodayCourseFragment(),
            FragmentType.NEWS to NewsFragment()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            preloadFragments()
        }
    }

    override fun onCreateContentView(): Int = R.layout.activity_main

    override fun onCreateViewModel(): MainDrawerViewModel = ViewModelProvider(this)[MainDrawerViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: MainDrawerViewModel) {
        drawer_main.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerOpened(drawerView: View) = viewModel.updateBalance()
        })
        nav_main.setNavigationItemSelectedListener(this)
        nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        showFragment(getViewModel().nowShowFragmentType)
    }

    private fun preloadFragments() {
        supportFragmentManager.beginTransaction().apply {
            for ((type, fragment) in FRAGMENTS) {
                if (fragment !in supportFragmentManager.fragments) {
                    add(R.id.fg_mainContent, fragment.apply {
                        arguments = Bundle().apply {
                            putInt(DrawerToolbarFragment.DRAWER_ID, R.id.drawer_main)
                        }
                    }, type.name)
                    attach(fragment)
                    hide(fragment)
                }
            }
        }.commitNow()
    }

    @Synchronized
    private fun showFragment(type: FragmentType) {
        if (type != getViewModel().nowShowFragmentType || !getViewModel().initFragmentShow()) {
            val oldFragment = supportFragmentManager.findFragmentByTag(getViewModel().nowShowFragmentType.name)
            supportFragmentManager.beginTransaction().apply {
                if (oldFragment != null) {
                    hide(oldFragment)
                }
                val newFragment = supportFragmentManager.findFragmentByTag(type.name)
                if (newFragment != null && newFragment in supportFragmentManager.fragments) {
                    show(newFragment)
                } else {
                    error("Fragment Not Preload Before Show!")
                }
            }.commit()
            getViewModel().nowShowFragmentType = type
        }
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
            nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).tv_cardBalance.text =
                getString(R.string.balance, String.format(Constants.KEEP_TWO_DECIMAL_PLACES, it))
        })
        viewModel.studentInfo.observe(this, Observer {
            nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).tv_userId.text = it.personalInfo.stuId.second
            nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).tv_userName.text = StudentInfo.trimExtra(it.personalInfo.name.second)
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