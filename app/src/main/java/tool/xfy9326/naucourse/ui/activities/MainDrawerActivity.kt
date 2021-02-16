package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.databinding.ActivityMainBinding
import tool.xfy9326.naucourse.databinding.ViewNavHeaderBinding
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
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
    private val fragmentTypeLock = Any()
    private var nowShowFragmentType: FragmentType? = null

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

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

    override fun onCreateContentView() = binding.root

    override fun onCreateViewModel(): MainDrawerViewModel = ViewModelProvider(this)[MainDrawerViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: MainDrawerViewModel) {
        binding.navMain.setNavigationItemSelectedListener(this)
        binding.navMain.getHeaderView(DEFAULT_NAV_HEADER_INDEX).setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
            binding.drawerMain.closeDrawers()
        }
        binding.navMain.getChildAt(0)?.isVerticalScrollBarEnabled = false
        setAdvancedFunctions()
    }

    private fun onUpdateNewVersion() {
        BaseIOUtils.deleteExternalFiles(Environment.DIRECTORY_DOWNLOADS)
        UpdateChecker.clearOldUpdatePref()
    }

    private fun setAdvancedFunctions() {
        val advancedFunctionSwitch = AppPref.EnableAdvancedFunctions
        if (advancedFunctionSwitch) {
            binding.drawerMain.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(newState: Int) {}

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

                override fun onDrawerClosed(drawerView: View) {}

                override fun onDrawerOpened(drawerView: View) = getViewModel().updateBalance()
            })
        }
        binding.navMain.menu.setGroupVisible(R.id.menu_groupAdvancedFunction, advancedFunctionSwitch)
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
        binding.drawerMain.postDelayed({
            when (item.itemId) {
                R.id.menu_navCourseArrange -> showFragment(FragmentType.COURSE_ARRANGE)
                R.id.menu_navCourseTable -> showFragment(FragmentType.COURSE_TABLE)
                R.id.menu_navNews -> showFragment(FragmentType.NEWS)
                R.id.menu_navSchoolCalendar -> openFunctionActivity(SchoolCalendarActivity::class.java)
                R.id.menu_navSchoolBus -> openFunctionActivity(SchoolBusActivity::class.java)
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
        binding.drawerMain.closeDrawers()
        return true
    }

    private fun <T> openFunctionActivity(clazz: Class<T>) = startActivity(Intent(this, clazz))

    private fun logout() {
        DialogUtils.createLogoutAttentionDialog(this, lifecycle) { _, _ ->
            FullScreenLoadingDialog.showDialog(supportFragmentManager)
            getViewModel().requestLogout()
        }.show()
    }

    override fun bindViewModel(viewModel: MainDrawerViewModel) {
        val headerBinding = ViewNavHeaderBinding.bind(binding.navMain.getHeaderView(DEFAULT_NAV_HEADER_INDEX))
        viewModel.studentCardBalance.observe(this, {
            headerBinding.tvCardBalanceOrClass.post {
                headerBinding.tvCardBalanceOrClass.text = getString(R.string.balance, String.format(BaseConst.KEEP_TWO_DECIMAL_PLACES, it))
            }
        })
        viewModel.studentInfo.observe(this, {

            binding.navMain.getHeaderView(DEFAULT_NAV_HEADER_INDEX).apply {
                headerBinding.tvUserId.text = it.personalInfo.stuId.second
                headerBinding.tvUserName.text = StudentInfo.trimExtra(it.personalInfo.name.second)
                if (!AppPref.EnableAdvancedFunctions) {
                    headerBinding.tvCardBalanceOrClass.text = it.personalInfo.currentClass.second
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
        NotifyBus[NotifyType.PASSWORD_ERROR].observeNotification(this) {
            startActivity(
                Intent(this, LoginActivity::class.java).putExtra(LoginActivity.INTENT_PASSWORD_ERROR_LOGIN, true)
            )
            finish()
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
    }

    override fun onBackPressed() {
        if (binding.drawerMain.isDrawerOpen(GravityCompat.START)) {
            binding.drawerMain.closeDrawer(GravityCompat.START)
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

    enum class FragmentType {
        COURSE_TABLE,
        COURSE_ARRANGE,
        NEWS
    }
}