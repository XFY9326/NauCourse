package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
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
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
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
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils

class MainDrawerActivity : ViewModelActivity<MainDrawerViewModel>(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val DEFAULT_NAV_HEADER_INDEX = 0
        private const val DOUBLE_PRESS_BACK_TIME = 1200L
        private const val NOW_SHOW_FRAGMENT_TYPE = "NOW_SHOW_FRAGMENT_TYPE"
    }

    private var lastRequestBackTime: Long = 0
    private var nightModeObserver: Observer<Event<Unit>>? = null
    private val fragmentTypeLock = Any()
    private var nowShowFragmentType: FragmentType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (savedInstanceState?.getSerializable(NOW_SHOW_FRAGMENT_TYPE) as FragmentType?)?.let {
            setNowShowFragment(it)
            getViewModel().initFragmentShow()
        }
        if (savedInstanceState == null) {
            fragmentInit()
            startAppInit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        nowShowFragmentType?.let {
            outState.putSerializable(NOW_SHOW_FRAGMENT_TYPE, it)
        }
        super.onSaveInstanceState(outState)
    }

    private fun startAppInit() {
        if (intent.getBooleanExtra(IntentUtils.NEW_VERSION_FLAG, false)) {
            onUpdateNewVersion()
        }
        if (BuildConfig.FLAVOR != OthersConst.FLAVOR_BETA && AppPref.ForceUpdateVersionCode > BuildConfig.VERSION_CODE) {
            showShortToast(R.string.force_update_attention)
        }
    }

    private fun fragmentInit() {
        preloadFragments()
        showFragment(getNowShowFragment())
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
        BaseIOUtils.deleteExternalFiles(Environment.DIRECTORY_DOWNLOADS)
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
                for (type in FragmentType.values()) {
                    createFragmentByType(type).let {
                        add(R.id.fg_mainContent, it, type.name)
                        attach(it)
                        hide(it)
                    }
                }
            }.commitNow()
        }
    }

    private fun createFragmentByType(type: FragmentType) =
        when (type) {
            FragmentType.COURSE_TABLE -> CourseTableFragment()
            FragmentType.COURSE_ARRANGE -> CourseArrangeFragment()
            FragmentType.NEWS -> NewsFragment()
        }.apply {
            arguments = Bundle().apply {
                putInt(DrawerToolbarFragment.DRAWER_ID, R.id.drawer_main)
            }
        }

    @Synchronized
    fun showFragment(type: FragmentType, withAnimation: Boolean = true) {
        val nowShowFragmentType = getNowShowFragment()
        if (type != nowShowFragmentType || !getViewModel().initFragmentShow()) {
            supportFragmentManager.beginTransaction().apply {
                if (withAnimation) setCustomAnimations(0, R.anim.fade_exit)
                supportFragmentManager.findFragmentByTag(nowShowFragmentType.name)?.let {
                    hide(it)
                }
                val newFragment = supportFragmentManager.findFragmentByTag(type.name)
                if (newFragment != null) {
                    show(newFragment)
                    setNowShowFragment(type)
                } else {
                    add(createFragmentByType(type), type.name)
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
                R.id.menu_navSchoolCalendar -> openFunctionActivity(SchoolCalendarActivity::class.java)
                R.id.menu_navLevelExam -> openFunctionActivity(LevelExamActivity::class.java)
                R.id.menu_navExamArrange -> openFunctionActivity(ExamArrangeActivity::class.java)
                R.id.menu_navSuspendCourseNotification -> openFunctionActivity(SuspendCourseActivity::class.java)
                R.id.menu_navEmptyRoomSearch -> openFunctionActivity(EmptyRoomSearchActivity::class.java)
                R.id.menu_navScoreQuery -> openFunctionActivity(ScoreQueryActivity::class.java)
                R.id.menu_navCourseManage -> openFunctionActivity(CourseManageActivity::class.java)
                R.id.menu_navSettings -> openFunctionActivity(SettingsActivity::class.java)
                R.id.menu_navLogout -> logout()
                R.id.menu_navExit -> finishAndRemoveTask()
            }
        }, 250)
        drawer_main.closeDrawers()
        return true
    }

    private fun <T> openFunctionActivity(clazz: Class<T>) {
        startActivity(Intent(this, clazz))
    }

    private fun logout() {
        DialogUtils.createLogoutAttentionDialog(this, lifecycle) { _, _ ->
            FullScreenLoadingDialog.showDialog(supportFragmentManager)
            getViewModel().requestLogout()
        }.show()
    }

    override fun bindViewModel(viewModel: MainDrawerViewModel) {
        viewModel.studentCardBalance.observe(this, {
            nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).tv_cardBalanceOrClass.post {
                tv_cardBalanceOrClass.text = getString(R.string.balance, String.format(BaseConst.KEEP_TWO_DECIMAL_PLACES, it))
            }
        })
        viewModel.studentInfo.observe(this, {
            nav_main.getHeaderView(DEFAULT_NAV_HEADER_INDEX).apply {
                tv_userId.text = it.personalInfo.stuId.second
                tv_userName.text = StudentInfo.trimExtra(it.personalInfo.name.second)
                if (!AppPref.EnableAdvancedFunctions) {
                    tv_cardBalanceOrClass.text = it.personalInfo.currentClass.second
                }
            }
        })
        viewModel.logoutSuccess.observeNotification(this) {
            FullScreenLoadingDialog.close(supportFragmentManager)
            BaseUtils.restartApplication()
            finish()
        }
        viewModel.updateInfo.observeEvent(this) {
            UpdateDialog.showDialog(supportFragmentManager, it)
        }
        NotifyBus[NotifyType.DEFAULT_ENTER_INTERFACE_CHANGED].observeNotification(this) {
            showFragment(getDefaultFragmentType())
        }
        NotifyBus[NotifyType.ADVANCED_FUNCTION_MODE_CHANGED].observeNotification(this) {
            setAdvancedFunctions()
            viewModel.updateBalance()
            viewModel.refreshPersonalInfo()
        }
        NotifyBus[NotifyType.COURSE_INIT_CONFLICT].observeNotification(this) {
            DialogUtils.createCourseInitConflictDialog(this, lifecycle).show()
        }
        // 需要Activity在后台时也监听夜间模式设定变化，防止延迟的界面更新
        tryRemoveNightModeObserver()
        nightModeObserver = NotifyBus[NotifyType.NIGHT_MODE_CHANGED].observeNotificationForever(MainDrawerActivity::class.java.simpleName) {
            recreate()
        }
    }

    override fun onBackPressed() {
        if (drawer_main.isDrawerOpen(GravityCompat.START)) {
            drawer_main.closeDrawer(GravityCompat.START)
        } else {
            if (SettingsPref.ExitApplicationDirectly) {
                val current = System.currentTimeMillis()
                if (current - lastRequestBackTime <= DOUBLE_PRESS_BACK_TIME) {
                    finishAndRemoveTask()
                } else {
                    showShortToast(R.string.double_press_exit)
                }
                lastRequestBackTime = current
            } else {
                moveTaskToBack(false)
            }
        }
    }

    @Synchronized
    private fun tryRemoveNightModeObserver() {
        if (nightModeObserver != null) {
            NotifyBus[NotifyType.NIGHT_MODE_CHANGED].removeObserver(nightModeObserver!!)
            nightModeObserver = null
        }
    }

    private fun setNowShowFragment(type: FragmentType) {
        synchronized(fragmentTypeLock) {
            nowShowFragmentType = type
        }
    }

    private fun getNowShowFragment(): FragmentType {
        synchronized(fragmentTypeLock) {
            if (nowShowFragmentType == null) {
                nowShowFragmentType = getDefaultFragmentType()
            }
            return nowShowFragmentType!!
        }
    }

    private fun getDefaultFragmentType() =
        when (SettingsPref.getDefaultEnterInterface()) {
            SettingsPref.EnterInterfaceType.COURSE_ARRANGE -> FragmentType.COURSE_ARRANGE
            SettingsPref.EnterInterfaceType.COURSE_TABLE -> FragmentType.COURSE_TABLE
            SettingsPref.EnterInterfaceType.NEWS -> FragmentType.NEWS
        }

    override fun onDestroy() {
        tryRemoveNightModeObserver()
        super.onDestroy()
    }

    enum class FragmentType {
        COURSE_TABLE,
        COURSE_ARRANGE,
        NEWS
    }
}