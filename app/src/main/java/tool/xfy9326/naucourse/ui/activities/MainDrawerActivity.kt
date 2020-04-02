package tool.xfy9326.naucourse.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_nav_header.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.livedata.Event
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.dialogs.FullScreenLoadingDialog
import tool.xfy9326.naucourse.ui.fragments.CourseArrangeFragment
import tool.xfy9326.naucourse.ui.fragments.CourseTableFragment
import tool.xfy9326.naucourse.ui.fragments.NewsFragment
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.activity.MainDrawerViewModel
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils

class MainDrawerActivity : ViewModelActivity<MainDrawerViewModel>(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val DEFAULT_NAV_HEADER_INDEX = 0

        private val FRAGMENTS = mapOf<FragmentType, DrawerToolbarFragment<*>>(
            FragmentType.COURSE_TABLE to CourseTableFragment(),
            FragmentType.COURSE_ARRANGE to CourseArrangeFragment(),
            FragmentType.NEWS to NewsFragment()
        )
    }

    private var nightModeObserver: Observer<in Event<Unit>>? = null

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
            drawer_main.closeDrawers()
        }
    }

    override fun onStart() {
        super.onStart()
        showFragment(getViewModel().getNowShowFragment())
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
    fun showFragment(type: FragmentType) {
        if (type != getViewModel().getNowShowFragment() || !getViewModel().initFragmentShow()) {
            val oldFragment = supportFragmentManager.findFragmentByTag(getViewModel().getNowShowFragment().name)
            supportFragmentManager.beginTransaction().apply {
                setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
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
            getViewModel().setNowShowFragment(type)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        lifecycleScope.launch(Dispatchers.Main) {
            drawer_main.closeDrawers()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            when (item.itemId) {
                R.id.menu_navCourseArrange -> showFragment(FragmentType.COURSE_ARRANGE)
                R.id.menu_navCourseTable -> showFragment(FragmentType.COURSE_TABLE)
                R.id.menu_navNews -> showFragment(FragmentType.NEWS)
                R.id.menu_navCourseEdit -> startActivity(Intent(this@MainDrawerActivity, CourseManageActivity::class.java))
                R.id.menu_navSettings -> startActivity(Intent(this@MainDrawerActivity, SettingsActivity::class.java))
                R.id.menu_navLogout -> logout()
                R.id.menu_navExit -> finishAndRemoveTask()
            }
        }
        return true
    }

    private fun logout() {
        DialogUtils.createLogoutAttentionDialog(this, lifecycle, DialogInterface.OnClickListener { _, _ ->
            FullScreenLoadingDialog().show(supportFragmentManager)
            getViewModel().requestLogout()
        }).show()
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
        viewModel.logoutSuccess.observeNotification(this, {
            FullScreenLoadingDialog.close(supportFragmentManager)
            BaseUtils.restartApplication(this)
        })
        // 需要Activity在后台时也监听夜间模式设定变化，防止延迟的界面更新
        tryRemoveNightModeObserver()
        nightModeObserver = NotifyBus[NotifyBus.Type.NIGHT_MODE_CHANGED].observeNotificationForever({
            recreate()
        }, MainDrawerActivity::class.java.simpleName)
    }

    override fun onBackPressed() {
        if (drawer_main.isDrawerOpen(GravityCompat.START)) {
            drawer_main.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(false)
        }
    }

    @Synchronized
    private fun tryRemoveNightModeObserver() {
        if (nightModeObserver != null) {
            NotifyBus[NotifyBus.Type.NIGHT_MODE_CHANGED].removeObserver(nightModeObserver!!)
            nightModeObserver = null
        }
    }

    override fun onDestroy() {
        tryRemoveNightModeObserver()
        super.onDestroy()
    }

    enum class FragmentType {
        COURSE_TABLE,
        COURSE_ARRANGE,
        NEWS,
        NONE
    }
}