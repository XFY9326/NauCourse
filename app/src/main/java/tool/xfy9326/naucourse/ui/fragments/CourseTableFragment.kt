package tool.xfy9326.naucourse.ui.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.iterator
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.ImageConst
import tool.xfy9326.naucourse.databinding.FragmentCourseTableBinding
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
import tool.xfy9326.naucourse.ui.dialogs.CourseDetailDialog
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourse.ui.views.viewpager.CourseTableViewPagerAdapter
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils

class CourseTableFragment : DrawerToolbarFragment<CourseTableViewModel>() {
    private lateinit var courseTableViewPagerAdapter: CourseTableViewPagerAdapter
    private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback

    private var _binding: FragmentCourseTableBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = view
        return if (v == null) {
            val binding = FragmentCourseTableBinding.inflate(layoutInflater, container, false).also {
                this._binding = it
            }
            binding.root
        } else {
            val parent = requireView().parent as ViewGroup?
            parent?.removeView(v)
            _binding = FragmentCourseTableBinding.bind(v)
            v
        }
    }

    override fun onCreateViewModel(): CourseTableViewModel = ViewModelProvider(this)[CourseTableViewModel::class.java]

    override fun onBindToolbar() = binding.tbCourseTable

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
        viewModel.getImageWhenCourseTableLoading.observeNotification(viewLifecycleOwner) {
            binding.layoutCourseTableWindow.showSnackBar(R.string.operation_when_data_loading)
        }
        viewModel.imageShareUri.observeEvent(viewLifecycleOwner) {
            startActivity(ShareUtils.getShareImageIntent(requireContext(), it))
        }
        viewModel.imageOperation.observeEvent(viewLifecycleOwner) {
            binding.layoutCourseTableWindow.showSnackBar(I18NUtils.getImageOperationTypeResId(it))
        }
        viewModel.nowShowWeekNum.observe(viewLifecycleOwner) {
            binding.tvNowShowWeekNum.text = getString(
                R.string.week_num,
                if (it <= 0) {
                    1
                } else {
                    it
                }
            )
            viewModel.requestShowWeekStatus(it)
        }
        viewModel.currentWeekStatus.observe(viewLifecycleOwner) {
            binding.tvNotCurrentWeek.setText(I18NUtils.getCurrentWeekStatusResId(it!!))
        }
        viewModel.todayDate.observe(viewLifecycleOwner) {
            binding.tvTodayDate.text = getString(R.string.today_date, it.first, it.second)
        }
        viewModel.maxWeekNum.observe(viewLifecycleOwner) {
            courseTableViewPagerAdapter.updateMaxWeekNum(it)
            AppPref.MaxWeekNumCache = it
        }
        viewModel.nowWeekNum.observe(viewLifecycleOwner) {
            updateNowWeekNum(viewModel, it)
        }
        viewModel.courseDetailInfo.observeEvent(viewLifecycleOwner) {
            CourseDetailDialog.showDialog(childFragmentManager, it)
        }
    }

    private fun bindGlobalObserver(viewModel: CourseTableViewModel) {
        NotifyBus[NotifyType.COURSE_STYLE_TERM_UPDATE].observeNotification(viewLifecycleOwner, CourseTableFragment::class.java.simpleName) {
            viewModel.refreshCourseData()
        }
        NotifyBus[NotifyType.COURSE_TERM_UPDATE].observeNotification(viewLifecycleOwner, CourseTableFragment::class.java.simpleName) {
            viewModel.refreshTimeInfo()
        }
        NotifyBus[NotifyType.REBUILD_COURSE_TABLE].observeNotification(viewLifecycleOwner, CourseTableFragment::class.java.simpleName) {
            viewModel.rebuildCourseTable()
        }
        NotifyBus[NotifyType.REBUILD_COURSE_TABLE_BACKGROUND].observeNotification(viewLifecycleOwner, CourseTableFragment::class.java.simpleName) {
            setCourseTableBackground()
            setFullScreenBackground(false)
        }
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
            binding.vpCourseTablePanel.setCurrentItem(showWeekNum - 1, false)
        }
    }

    private fun shareCourseTable() {
        showShortToast(R.string.generating_image)
        getViewModel().createShareImage(requireContext(), binding.vpCourseTablePanel.currentItem + 1, resources.displayMetrics.widthPixels)
    }

    override fun initView(viewModel: CourseTableViewModel) {
        setToolbarTitleEnabled(false)
        setCourseTableBackground()
        setFullScreenBackground(true)

        courseTableViewPagerAdapter = CourseTableViewPagerAdapter(this, AppPref.MaxWeekNumCache)

        binding.vpCourseTablePanel.adapter = courseTableViewPagerAdapter
        binding.vpCourseTablePanel.offscreenPageLimit = 1
        viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.nowShowWeekNum.value = position + 1
            }
        }
        binding.vpCourseTablePanel.registerOnPageChangeCallback(viewPagerCallback)

        binding.layoutDateInfoBar.setOnClickListener {
            turnToDefaultWeek(viewModel)
        }
    }

    private fun setCourseTableBackground() {
        if (SettingsPref.CustomCourseTableBackground) {
            AppPref.CourseTableBackgroundImageName?.let {
                val imageFile = ImageUtils.getLocalImageFile(it, ImageConst.DIR_APP_IMAGE)
                if (imageFile?.exists() == true) {
                    binding.ivCourseTableBackground.apply {
                        alpha = SettingsPref.CourseTableBackgroundAlpha / 100f
                        scaleType = SettingsPref.getCourseTableBackgroundScareType()
                        visibility = View.VISIBLE
                    }
                    Glide.with(this@CourseTableFragment).load(imageFile).transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.ivCourseTableBackground)
                    return
                }
            }
        }
        binding.ivCourseTableBackground.visibility = View.GONE
    }

    private fun setFullScreenBackground(isInit: Boolean) {
        if (SettingsPref.CustomCourseTableBackground && SettingsPref.CourseTableBackgroundFullScreen) {
            enableFullScreenBackground()
        } else {
            disableFullScreenBackground(isInit)
        }
    }

    private fun enableFullScreenBackground() {
        binding.apply {
            layoutCourseTableWindow.fitsSystemWindows = false
            ViewCompat.requestApplyInsets(layoutCourseTableWindow)
            layoutCourseTableWindow.setPadding(0)

            layoutCourseTableAppBar.fitsSystemWindows = true
            ViewCompat.requestApplyInsets(layoutCourseTableAppBar)

            ivCourseTableBackground.apply {
                layoutParams = CoordinatorLayout.LayoutParams(layoutParams).apply {
                    setMargins(0)
                }
            }
            layoutCourseTableAppBar.apply {
                outlineProvider = null
                background = null
                alpha = SettingsPref.CustomCourseTableAlpha / 100f
            }
            tbCourseTable.background = null

            val colorTimeText =
                if (SettingsPref.EnableCourseTableTimeTextColor) {
                    SettingsPref.CourseTableTimeTextColor
                } else {
                    ContextCompat.getColor(requireContext(), R.color.colorCourseTimeDefault)
                }

            tvNowShowWeekNum.setTextColor(colorTimeText)
            tvTodayDate.setTextColor(colorTimeText)
            tvNotCurrentWeek.setTextColor(colorTimeText)
            tbCourseTable.navigationIcon?.colorFilter = PorterDuffColorFilter(colorTimeText, PorterDuff.Mode.SRC)
            tbCourseTable.menu.iterator().forEach {
                it.icon?.colorFilter = PorterDuffColorFilter(colorTimeText, PorterDuff.Mode.SRC_ATOP)
            }
        }
    }

    private fun disableFullScreenBackground(isInit: Boolean) {
        binding.apply {
            layoutCourseTableWindow.fitsSystemWindows = true
            ViewCompat.requestApplyInsets(layoutCourseTableWindow)

            layoutCourseTableAppBar.fitsSystemWindows = false
            ViewCompat.requestApplyInsets(layoutCourseTableAppBar)

            if (!isInit) {
                ivCourseTableBackground.apply {
                    layoutParams = CoordinatorLayout.LayoutParams(layoutParams).apply {
                        setMargins(0, ViewUtils.getActionBarSize(requireContext()), 0, 0)
                    }
                }
                layoutCourseTableAppBar.apply {
                    outlineProvider = ViewOutlineProvider.BOUNDS
                    setBackgroundResource(R.color.colorPrimary)
                    alpha = 1f
                }
                tbCourseTable.setBackgroundResource(R.color.colorPrimary)

                tvNowShowWeekNum.setTextColor(Color.WHITE)
                tvTodayDate.setTextColor(Color.WHITE)
                tvNotCurrentWeek.setTextColor(Color.WHITE)
                tbCourseTable.navigationIcon?.clearColorFilter()
                tbCourseTable.menu.iterator().forEach {
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
                binding.vpCourseTablePanel.setCurrentItem(0, true)
            } else {
                if (showAhead == true) {
                    binding.vpCourseTablePanel.setCurrentItem(defaultWeek, true)
                } else {
                    binding.vpCourseTablePanel.setCurrentItem(defaultWeek - 1, true)
                }
            }
        }
    }

    private fun showCourseTableControlPanel() {
        DialogUtils.createCourseTableControlDialog(
            requireContext(), viewLifecycleOwner.lifecycle, getViewModel().currentWeekNum ?: 0, binding.vpCourseTablePanel.currentItem + 1,
            AppPref.MaxWeekNumCache
        ) {
            binding.vpCourseTablePanel.setCurrentItem(it - 1, true)
        }.show()
    }

    override fun onDestroyView() {
        binding.vpCourseTablePanel.unregisterOnPageChangeCallback(viewPagerCallback)
        super.onDestroyView()
        _binding = null
    }
}