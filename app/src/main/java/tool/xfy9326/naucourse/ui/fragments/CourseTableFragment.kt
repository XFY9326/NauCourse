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
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_course_table.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCell
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.ui.dialogs.CourseDetailDialog
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.table.CourseTableViewHelper
import tool.xfy9326.naucourse.ui.views.table.OnCourseCellClickListener
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils

class CourseTableFragment : DrawerToolbarFragment<CourseTableViewModel>(),
    OnCourseCellClickListener {
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
        viewModel.getImageWhenCourseTableLoading.observeNotification(this, {
            ActivityUtils.showSnackBar(layout_courseTableWindow, R.string.operation_when_data_loading)
        })
        viewModel.imageShareUri.observeEvent(this, Observer {
            startActivity(ShareUtils.getShareImageIntent(requireContext(), it))
        })
        viewModel.imageOperation.observeEvent(this, Observer {
            ActivityUtils.showSnackBar(layout_courseTableWindow, I18NUtils.getImageOperationTypeResId(it))
        })
        viewModel.nowShowWeekNum.observe(viewLifecycleOwner, Observer {
            tv_nowShowWeekNum.text = getString(R.string.week_num, it)
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
            synchronized(this) {
                if (!viewModel.hasInitWithNowWeekNum) {
                    viewModel.hasInitWithNowWeekNum = true
                    val showWeekNum = if (it.first == 0) 1 else if (it.second) it.first + 1 else it.first
                    viewModel.nowShowWeekNum.postValue(showWeekNum - 1)
                    vp_courseTablePanel.setCurrentItem(showWeekNum - 1, false)
                }
            }
        })
        viewModel.courseDetailInfo.observeEvent(viewLifecycleOwner, Observer {
            CourseDetailDialog.showDialog(childFragmentManager, it)
        })
        viewModel.courseTableBackground.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                iv_courseTableBackground.setImageBitmap(it)
                iv_courseTableBackground.alpha = SettingsPref.CourseTableBackgroundAlpha / 100f
                iv_courseTableBackground.scaleType = SettingsPref.getCourseTableBackgroundScareType()
                iv_courseTableBackground.visibility = View.VISIBLE
            } else {
                iv_courseTableBackground.visibility = View.GONE
            }
        })

        NotifyBus[NotifyBus.Type.COURSE_STYLE_TERM_UPDATE].observeNotification(viewLifecycleOwner, {
            viewModel.refreshCourseData()
        }, CourseTableFragment::class.java.simpleName)
        NotifyBus[NotifyBus.Type.COURSE_TERM_UPDATE].observeNotification(viewLifecycleOwner, {
            viewModel.refreshTimeInfo()
        }, CourseTableFragment::class.java.simpleName)
        NotifyBus[NotifyBus.Type.REBUILD_COURSE_TABLE].observeNotification(viewLifecycleOwner, {
            viewModel.rebuildCourseTable()
        }, CourseTableFragment::class.java.simpleName)
        NotifyBus[NotifyBus.Type.REBUILD_COURSE_TABLE_BACKGROUND].observeNotification(viewLifecycleOwner, {
            viewModel.requestCourseTableBackground()
            setFullScreenBackground(false)
        }, CourseTableFragment::class.java.simpleName)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            if (SettingsPref.AutoAsyncCourseData) {
                getViewModel().startOnlineDataAsync()
            }
        }
        super.onHiddenChanged(hidden)
    }

    private fun shareCourseTable() {
        ActivityUtils.showSnackBar(layout_courseTableWindow, R.string.generating_image)
        getViewModel().createShareImage(requireContext(), vp_courseTablePanel.currentItem + 1, resources.displayMetrics.widthPixels)
    }

    override fun initView(viewModel: CourseTableViewModel) {
        setToolbarTitleEnabled(false)
        CourseTableViewHelper.setOnCourseCellClickListener(this)

        courseTableViewPagerAdapter = CourseTableViewPagerAdapter(this, AppPref.MaxWeekNumCache)

        vp_courseTablePanel.adapter = courseTableViewPagerAdapter
        vp_courseTablePanel.offscreenPageLimit = 1
        viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.nowShowWeekNum.postValue(position + 1)
            }
        }
        vp_courseTablePanel.registerOnPageChangeCallback(viewPagerCallback)

        layout_dateInfoBar.setOnClickListener {
            turnToDefaultWeek(viewModel)
        }

        viewModel.requestCourseTableBackground()
        setFullScreenBackground(true)
    }

    private fun setFullScreenBackground(isInit: Boolean) {
        if (SettingsPref.CustomCourseTableBackground && SettingsPref.CourseTableBackgroundFullScreen) {
            layout_courseTableWindow.fitsSystemWindows = false
            ViewCompat.requestApplyInsets(layout_courseTableWindow)
            layout_courseTableWindow.setPadding(0)

            layout_courseTableAppBar.fitsSystemWindows = true
            ViewCompat.requestApplyInsets(layout_courseTableAppBar)

            iv_courseTableBackground.layoutParams = CoordinatorLayout.LayoutParams(iv_courseTableBackground.layoutParams).apply {
                setMargins(0, 0, 0, 0)
            }
            layout_courseTableAppBar.apply {
                outlineProvider = null
                background = null
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
        } else {
            layout_courseTableWindow.fitsSystemWindows = true
            ViewCompat.requestApplyInsets(layout_courseTableWindow)

            layout_courseTableAppBar.fitsSystemWindows = false
            ViewCompat.requestApplyInsets(layout_courseTableAppBar)

            if (!isInit) {
                iv_courseTableBackground.layoutParams = CoordinatorLayout.LayoutParams(iv_courseTableBackground.layoutParams).apply {
                    setMargins(0, ViewUtils.getActionBarSize(requireContext()), 0, 0)
                }
                layout_courseTableAppBar.apply {
                    outlineProvider = ViewOutlineProvider.BOUNDS
                    setBackgroundResource(R.color.colorPrimary)
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

    override fun onCourseCellClick(courseCell: CourseCell, cellStyle: CourseCellStyle) {
        getViewModel().requestCourseDetailInfo(courseCell, cellStyle)
    }

    override fun onDestroyView() {
        vp_courseTablePanel.unregisterOnPageChangeCallback(viewPagerCallback)
        super.onDestroyView()
    }

    override fun onDestroy() {
        getViewModel().courseTableBackground.value = null
        super.onDestroy()
    }
}