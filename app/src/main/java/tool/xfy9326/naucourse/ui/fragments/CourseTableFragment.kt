package tool.xfy9326.naucourse.ui.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.iterator
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.fragment_course_table.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.ImageConst
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
import tool.xfy9326.naucourse.ui.dialogs.CourseDetailDialog
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils

class CourseTableFragment : DrawerToolbarFragment<CourseTableViewModel>() {
    private lateinit var courseTableViewPagerAdapter: CourseTableViewPagerAdapter
    private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateContentView(): Int = R.layout.fragment_course_table

    override fun onCreateViewModel(): CourseTableViewModel = ViewModelProvider(this)[CourseTableViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_courseTable

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_courseTableControl -> showCourseTableControlPanel()
            R.id.menu_courseTableShare -> shareCourseTable()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun bindViewModel(viewModel: CourseTableViewModel) {
        bindLocalObserver(viewModel)
        bindGlobalObserver(viewModel)
    }

    private fun bindLocalObserver(viewModel: CourseTableViewModel) {
        viewModel.getImageWhenCourseTableLoading.observeNotification(viewLifecycleOwner, {
            ActivityUtils.showSnackBar(layout_courseTableWindow, R.string.operation_when_data_loading)
        })
        viewModel.imageShareUri.observeEvent(viewLifecycleOwner, Observer {
            startActivity(ShareUtils.getShareImageIntent(requireContext(), it))
        })
        viewModel.imageOperation.observeEvent(viewLifecycleOwner, Observer {
            ActivityUtils.showSnackBar(layout_courseTableWindow, I18NUtils.getImageOperationTypeResId(it))
        })
        viewModel.nowShowWeekNum.observe(viewLifecycleOwner, Observer {
            tv_nowShowWeekNum.text = getString(
                R.string.week_num,
                if (it <= 0) {
                    1
                } else {
                    it
                }
            )

            viewModel.requestShowWeekStatus(it)
        })
        viewModel.currentWeekStatus.observe(viewLifecycleOwner, Observer {
            tv_notCurrentWeek.setText(I18NUtils.getCurrentWeekStatusResId(it!!))
        })
        viewModel.todayDate.observe(viewLifecycleOwner, Observer {
            tv_todayDate.text = getString(R.string.today_date, it.first, it.second)
        })
        viewModel.maxWeekNum.observe(viewLifecycleOwner, Observer {
            courseTableViewPagerAdapter.updateMaxWeekNum(it)
            AppPref.MaxWeekNumCache = it
        })
        viewModel.nowWeekNum.observe(viewLifecycleOwner, Observer {
            updateNowWeekNum(viewModel, it)
        })
        viewModel.courseDetailInfo.observeEvent(viewLifecycleOwner, Observer {
            CourseDetailDialog.showDialog(childFragmentManager, it)
        })
    }

    private fun bindGlobalObserver(viewModel: CourseTableViewModel) {
        NotifyBus[NotifyType.COURSE_STYLE_TERM_UPDATE].observeNotification(viewLifecycleOwner, {
            viewModel.refreshCourseData()
        }, CourseTableFragment::class.java.simpleName)
        NotifyBus[NotifyType.COURSE_TERM_UPDATE].observeNotification(viewLifecycleOwner, {
            viewModel.refreshTimeInfo()
        }, CourseTableFragment::class.java.simpleName)
        NotifyBus[NotifyType.REBUILD_COURSE_TABLE].observeNotification(viewLifecycleOwner, {
            viewModel.rebuildCourseTable()
        }, CourseTableFragment::class.java.simpleName)
        NotifyBus[NotifyType.REBUILD_COURSE_TABLE_BACKGROUND].observeNotification(viewLifecycleOwner, {
            setCourseTableBackground()
            setFullScreenBackground(false)
        }, CourseTableFragment::class.java.simpleName)
    }

    override fun onStart() {
        getViewModel().startOnlineDataAsync()
        super.onStart()
    }

    @Synchronized
    private fun updateNowWeekNum(viewModel: CourseTableViewModel, weekNum: Pair<Int, Boolean>) {
        if (!viewModel.hasInitWithNowWeekNum) {
            viewModel.hasInitWithNowWeekNum = true
            val showWeekNum = if (weekNum.first == 0) 1 else if (weekNum.second) weekNum.first + 1 else weekNum.first
            viewModel.nowShowWeekNum.value = showWeekNum - 1
            vp_courseTablePanel.setCurrentItem(showWeekNum - 1, false)
        }
    }

    private fun shareCourseTable() {
        showToast(R.string.generating_image)
        getViewModel().createShareImage(requireContext(), vp_courseTablePanel.currentItem + 1, resources.displayMetrics.widthPixels)
    }

    override fun initView(viewModel: CourseTableViewModel) {
        setToolbarTitleEnabled(false)
        setCourseTableBackground()
        setFullScreenBackground(true)

        courseTableViewPagerAdapter = CourseTableViewPagerAdapter(this, AppPref.MaxWeekNumCache)

        vp_courseTablePanel.adapter = courseTableViewPagerAdapter
        vp_courseTablePanel.offscreenPageLimit = 1
        viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.nowShowWeekNum.value = position + 1
            }
        }
        vp_courseTablePanel.registerOnPageChangeCallback(viewPagerCallback)

        layout_dateInfoBar.setOnClickListener {
            turnToDefaultWeek(viewModel)
        }
    }

    private fun setCourseTableBackground() {
        if (SettingsPref.CustomCourseTableBackground) {
            AppPref.CourseTableBackgroundImageName?.let {
                val imageFile = ImageUtils.getLocalImageFile(it, ImageConst.DIR_APP_IMAGE)
                if (imageFile?.exists() == true) {
                    iv_courseTableBackground.apply {
                        alpha = SettingsPref.CourseTableBackgroundAlpha / 100f
                        scaleType = SettingsPref.getCourseTableBackgroundScareType()
                        visibility = View.VISIBLE
                    }
                    Glide.with(this@CourseTableFragment).load(imageFile).transition(DrawableTransitionOptions.withCrossFade())
                        .into(iv_courseTableBackground)
                    return
                }
            }
        }
        iv_courseTableBackground.visibility = View.GONE
    }

    private fun setFullScreenBackground(isInit: Boolean) {
        if (SettingsPref.CustomCourseTableBackground && SettingsPref.CourseTableBackgroundFullScreen) {
            enableFullScreenBackground()
        } else {
            disableFullScreenBackground(isInit)
        }
    }

    private fun enableFullScreenBackground() {
        layout_courseTableWindow.fitsSystemWindows = false
        ViewCompat.requestApplyInsets(layout_courseTableWindow)
        layout_courseTableWindow.setPadding(0)

        layout_courseTableAppBar.fitsSystemWindows = true
        ViewCompat.requestApplyInsets(layout_courseTableAppBar)

        iv_courseTableBackground.apply {
            layoutParams = CoordinatorLayout.LayoutParams(layoutParams).apply {
                setMargins(0)
            }
        }
        layout_courseTableAppBar.apply {
            outlineProvider = null
            background = null
            alpha = SettingsPref.CustomCourseTableAlpha / 100f
        }
        tb_courseTable.background = null

        val colorTimeText =
            if (SettingsPref.EnableCourseTableTimeTextColor) {
                SettingsPref.CourseTableTimeTextColor
            } else {
                ContextCompat.getColor(requireContext(), R.color.colorCourseTimeDefault)
            }

        tv_nowShowWeekNum.setTextColor(colorTimeText)
        tv_todayDate.setTextColor(colorTimeText)
        tv_notCurrentWeek.setTextColor(colorTimeText)
        tb_courseTable.navigationIcon?.colorFilter = PorterDuffColorFilter(colorTimeText, PorterDuff.Mode.SRC)
        tb_courseTable.menu.iterator().forEach {
            it.icon?.colorFilter = PorterDuffColorFilter(colorTimeText, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun disableFullScreenBackground(isInit: Boolean) {
        layout_courseTableWindow.fitsSystemWindows = true
        ViewCompat.requestApplyInsets(layout_courseTableWindow)

        layout_courseTableAppBar.fitsSystemWindows = false
        ViewCompat.requestApplyInsets(layout_courseTableAppBar)

        if (!isInit) {
            iv_courseTableBackground.apply {
                layoutParams = CoordinatorLayout.LayoutParams(layoutParams).apply {
                    setMargins(0, ViewUtils.getActionBarSize(requireContext()), 0, 0)
                }
            }
            layout_courseTableAppBar.apply {
                outlineProvider = ViewOutlineProvider.BOUNDS
                setBackgroundResource(R.color.colorPrimary)
                alpha = 1f
            }
            tb_courseTable.setBackgroundResource(R.color.colorPrimary)

            tv_nowShowWeekNum.setTextColor(Color.WHITE)
            tv_todayDate.setTextColor(Color.WHITE)
            tv_notCurrentWeek.setTextColor(Color.WHITE)
            tb_courseTable.navigationIcon?.clearColorFilter()
            tb_courseTable.menu.iterator().forEach {
                it.icon?.clearColorFilter()
            }
        }
    }

    private fun turnToDefaultWeek(viewModel: CourseTableViewModel) {
        val defaultWeek = viewModel.currentWeekNum
        val showAhead = viewModel.showNextWeekAhead
        if (defaultWeek != null) {
            if (defaultWeek == 0) {
                vp_courseTablePanel.setCurrentItem(0, true)
            } else {
                if (showAhead == true) {
                    vp_courseTablePanel.setCurrentItem(defaultWeek, true)
                } else {
                    vp_courseTablePanel.setCurrentItem(defaultWeek - 1, true)
                }
            }
        }
    }

    private fun showCourseTableControlPanel() {
        DialogUtils.createCourseTableControlDialog(
            requireContext(), viewLifecycleOwner.lifecycle, getViewModel().currentWeekNum ?: 0, vp_courseTablePanel.currentItem + 1,
            AppPref.MaxWeekNumCache
        ) {
            vp_courseTablePanel.setCurrentItem(it - 1, true)
        }.show()
    }

    override fun onDestroyView() {
        vp_courseTablePanel.unregisterOnPageChangeCallback(viewPagerCallback)
        super.onDestroyView()
    }
}