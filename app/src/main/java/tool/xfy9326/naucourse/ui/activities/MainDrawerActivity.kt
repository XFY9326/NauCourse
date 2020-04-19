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
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_nav_header.*
import kotlinx.android.synthetic.main.view_nav_header.view.*
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.livedata.Event
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.dialogs.FullScreenLoadingDialog
import tool.xfy9326.naucourse.ui.dialogs.UpdateDialog
import tool.xfy9326.naucourse.ui.fragments.CourseArrangeFragment
import tool.xfy9326.naucourse.ui.fragments.CourseTableFragment
import tool.xfy9326.naucourse.ui.fragments.NewsFragment
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.activity.MainDrawerViewModel
import tool.xfy9326.naucourse.update.UpdateChecker
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
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
            showFragment(getViewModel().getNowShowFragment())
        }
        if (savedInstanceState == null) {
            if (intent.getBooleanExtra(IntentUtils.NEW_VERSION_FLAG, false)) {
                onUpdateNewVersion()
            }
            if (AppPref.ForceUpdateVersionCode > BuildConfig.VERSION_CODE) {
                showToast(R.string.force_update_attention)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (SettingsPref.AutoCheckUpdates) {
            getViewModel().checkUpdate()
        }
    }

    override fun onCreateContentView(): Int = R.layout.activity_main

    override fun onCreateViewModel(): MainDrawerViewModel = ViewModelProvider(this)[MainDrawerViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: MainDrawerViewModel) {
        nav_main.setNavigationItemSelectedListener(this)
        nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
            drawer_main.closeDrawers()
        }
        nav_main.getChildAt(0)?.isVerticalScrollBarEnabled = false
        setAdvancedFunctions()
    }

    private fun onUpdateNewVersion() {
        UpdateChecker.clearOldUpdatePref()
    }

    private fun setAdvancedFunctions() {
        val advancedFunctionSwitch = AppPref.EnableAdvancedFunctions
        if (advancedFunctionSwitch) {
            drawer_main.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(newState: Int) {}

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

                override fun onDrawerClosed(drawerView: View) {}

                override fun onDrawerOpened(drawerView: View) = getViewModel().updateBalance()
            })
        }
        nav_main.menu.setGroupVisible(R.id.menu_groupAdvancedFunction, advancedFunctionSwitch)
    }

    private fun preloadFragments() {
        if (supportFragmentManager.fragments.isEmpty()) {
            supportFragmentManager.beginTransaction().apply {
                for ((type, fragment) in FRAGMENTS) {
                    add(R.id.fg_mainContent, fragment.apply {
                        arguments = Bundle().apply {
                            putInt(DrawerToolbarFragment.DRAWER_ID, R.id.drawer_main)
                        }
                    }, type.name)
                    hide(fragment)
                }
            }.commitNow()
        }
    }

    @Synchronized
    fun showFragment(type: FragmentType, withAnimation: Boolean = true) {
        if (type != getViewModel().getNowShowFragment() || !getViewModel().initFragmentShow()) {
            val oldFragment = supportFragmentManager.findFragmentByTag(getViewModel().getNowShowFragment().name)
            supportFragmentManager.beginTransaction().apply {
                if (withAnimation) setCustomAnimations(0, R.anim.fade_exit)
                if (oldFragment != null) {
                    hide(oldFragment)
                }
                val newFragment = supportFragmentManager.findFragmentByTag(type.name)
                if (newFragment != null) {
                    show(newFragment)
                    getViewModel().setNowShowFragment(type)
                } else {
                    error("Fragment Not Preload Before Show!")
                }
            }.commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_main.postDelayed({
            when (item.itemId) {
                R.id.menu_navCourseArrange -> showFragment(FragmentType.COURSE_ARRANGE)
                R.id.menu_navCourseTable -> showFragment(FragmentType.COURSE_TABLE)
                R.id.menu_navNews -> showFragment(FragmentType.NEWS)
                R.id.menu_navSchoolCalendar -> startActivity(Intent(this@MainDrawerActivity, SchoolCalendarActivity::class.java))
                R.id.menu_navLevelExam -> startActivity(Intent(this@MainDrawerActivity, LevelExamActivity::class.java))
                R.id.menu_navExamArrange -> startActivity(Intent(this@MainDrawerActivity, ExamArrangeActivity::class.java))
                R.id.menu_navScoreQuery -> startActivity(Intent(this@MainDrawerActivity, ScoreQueryActivity::class.java))
                R.id.menu_navCourseManage -> startActivity(Intent(this@MainDrawerActivity, CourseManageActivity::class.java))
                R.id.menu_navSettings -> startActivity(Intent(this@MainDrawerActivity, SettingsActivity::class.java))
                R.id.menu_navLogout -> logout()
                R.id.menu_navExit -> finishAndRemoveTask()
            }
        }, 250)
        drawer_main.closeDrawers()
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
            nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).tv_cardBalanceOrClass.post {
                tv_cardBalanceOrClass.text = getString(R.string.balance, String.format(Constants.KEEP_TWO_DECIMAL_PLACES, it))
            }
        })
        viewModel.studentInfo.observe(this, Observer {
            nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).apply {
                tv_userId.text = it.personalInfo.stuId.second
                tv_userName.text = StudentInfo.trimExtra(it.personalInfo.name.second)
                if (!AppPref.EnableAdvancedFunctions) {
                    tv_cardBalanceOrClass.text = it.personalInfo.currentClass.second
                }
            }
        })
        viewModel.logoutSuccess.observeNotification(this, {
            FullScreenLoadingDialog.close(supportFragmentManager)
            BaseUtils.restartApplication()
            finish()
        })
        viewModel.updateInfo.observeEvent(this, Observer {
            UpdateDialog.showDialog(supportFragmentManager, it)
        })
        NotifyBus[NotifyBus.Type.ADVANCED_FUNCTION_MODE_CHANGED].observe(this, Observer {
            setAdvancedFunctions()
            viewModel.updateBalance()
            viewModel.refreshPersonalInfo()
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
            if (SettingsPref.ExitApplicationDirectly) {
                finishAndRemoveTask()
            } else {
                moveTaskToBack(false)
            }
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