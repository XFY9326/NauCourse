package tool.xfy9326.naucourse.ui.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseBundle
import tool.xfy9326.naucourse.beans.CourseItem
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.databinding.FragmentCourseArrangeBinding
import tool.xfy9326.naucourse.databinding.ViewListCourseItemBinding
import tool.xfy9326.naucourse.databinding.ViewListCourseSimpleItemBinding
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
import tool.xfy9326.naucourse.ui.activities.MainDrawerActivity
import tool.xfy9326.naucourse.ui.dialogs.CourseDetailDialog
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.fragment.CourseArrangeViewModel
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedLinearLayout
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

class CourseArrangeFragment : DrawerToolbarFragment<CourseArrangeViewModel>() {
    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)
        private val DATE_FORMAT_HM = SimpleDateFormat(TimeConst.FORMAT_HM, Locale.CHINA)
    }

    private var _binding: FragmentCourseArrangeBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = view
        return if (v == null) {
            val binding = FragmentCourseArrangeBinding.inflate(layoutInflater, container, false).also {
                this._binding = it
            }
            binding.root
        } else {
            val parent = requireView().parent as ViewGroup?
            parent?.removeView(v)
            _binding = FragmentCourseArrangeBinding.bind(v)
            v
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateViewModel(): CourseArrangeViewModel = ViewModelProvider(this)[CourseArrangeViewModel::class.java]

    override fun onBindToolbar(): Toolbar = binding.toolbar.tbGeneral.apply {
        title = getString(R.string.course_arrange)
    }

    override fun onStart() {
        if (SettingsPref.AutoUpdateCourseArrange) {
            getViewModel().refreshArrangeCourses(false)
        }
        super.onStart()
    }

    override fun initView(viewModel: CourseArrangeViewModel) {
        binding.aslTodayCourse.setOnRefreshListener {
            viewModel.refreshArrangeCourses(true)
        }
        binding.todayCourse.tvTodayCourseMore.setOnClickListener {
            (requireActivity() as MainDrawerActivity).showFragment(MainDrawerActivity.FragmentType.COURSE_TABLE)
        }
        if (!SettingsPref.AutoUpdateCourseArrange) {
            getViewModel().refreshArrangeCourses(false)
        }
    }

    override fun bindViewModel(viewModel: CourseArrangeViewModel) {
        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.aslTodayCourse.postStopRefreshing()
        }
        viewModel.notifyMsg.observeEvent(viewLifecycleOwner) {
            binding.layoutTodayCourse.showSnackBar(I18NUtils.getTodayCourseNotifyTypeResId(it))
        }
        viewModel.todayCourses.observe(viewLifecycleOwner) {
            buildListCourseItem(binding.todayCourse.layoutTodayCourseContent, binding.todayCourse.tvTodayCourseEmpty, it)
        }
        viewModel.tomorrowCourses.observe(viewLifecycleOwner) {
            buildListCourseItem(binding.tomorrowCourse.layoutTomorrowCourseContent, binding.tomorrowCourse.tvTomorrowCourseEmpty, it)
        }
        viewModel.notThisWeekCourse.observe(viewLifecycleOwner) {
            buildListCourseItem(
                binding.notThisWeekCourse.layoutNotThisWeekCourseContent,
                binding.notThisWeekCourse.tvNotThisWeekCourseEmpty,
                it,
                true
            )
        }
        viewModel.nextCourseData.observe(viewLifecycleOwner) {
            buildNextCourse(it)
        }
        viewModel.nextCourseBundle.observeEvent(viewLifecycleOwner) {
            if (it == null) {
                AppWidgetUtils.refreshNextCourseWidget(requireContext())
            } else {
                AppWidgetUtils.updateNextCourseWidget(requireContext(), it)
            }
        }
        viewModel.termDateData.observe(viewLifecycleOwner) {
            buildTermDate(it)
        }
        viewModel.courseDetail.observeEvent(viewLifecycleOwner) {
            CourseDetailDialog.showDialog(childFragmentManager, it)
        }
        val notifyObserver = {
            getViewModel().refreshArrangeCourses(showAttention = false, updateNextCourseWidget = false)
            viewModel.nextCourseBundle.postEventValue(null)
            IntentUtils.refreshNextCourseAlarmData(requireContext())
        }
        NotifyBus[NotifyType.COURSE_ASYNC_UPDATE].observeNotification(
            viewLifecycleOwner,
            CourseArrangeFragment::class.java.simpleName,
            notifyObserver
        )
        NotifyBus[NotifyType.COURSE_STYLE_TERM_UPDATE].observeNotification(
            viewLifecycleOwner,
            CourseArrangeFragment::class.java.simpleName,
            notifyObserver
        )
    }

    private fun buildTermDate(termDate: TermDate?) {
        binding.nextCourse.apply {
            if (termDate == null) {
                tvTodayCourseDate.text = DATE_FORMAT_YMD.format(Date())

                tvTodayCourseTerm.visibility = View.GONE
                tvTodayCourseWeekNum.visibility = View.GONE
                tvTodayCourseDate.visibility = View.VISIBLE
            } else {
                val term = termDate.getTerm()
                tvTodayCourseTerm.text = getString(R.string.term, term.startYear, term.endYear, term.termNum)
                if (termDate.inVacation) {
                    tvTodayCourseWeekNum.text = getString(R.string.in_vacation)
                } else {
                    tvTodayCourseWeekNum.text = getString(R.string.week_num, termDate.currentWeekNum)
                }
                tvTodayCourseTerm.visibility = View.VISIBLE
                tvTodayCourseWeekNum.visibility = View.VISIBLE
                tvTodayCourseDate.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildNextCourse(courseItem: CourseItem?) {
        binding.nextCourse.apply {
            if (courseItem == null) {
                layoutNextCourse.visibility = View.GONE
                layoutNextCourseBreak.visibility = View.VISIBLE
            } else if (courseItem.detail != null) {
                tvNextCourseName.text = courseItem.course.name
                tvNextCourseTime.text = DATE_FORMAT_HM.format(courseItem.detail.dateTimePeriod.startDateTime) + "~" +
                        DATE_FORMAT_HM.format(courseItem.detail.dateTimePeriod.endDateTime)
                tvNextCourseDetail.text =
                    ViewUtils.getCourseDataShowText("${courseItem.courseTime.location}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${courseItem.course.teacher}")

                layoutNextCourse.visibility = View.VISIBLE
                layoutNextCourseBreak.visibility = View.GONE
            }
        }
    }

    private fun buildListCourseItem(container: AdvancedLinearLayout, emptyView: View, courses: Array<CourseBundle>, noDetail: Boolean = false) {
        if (courses.isEmpty()) {
            container.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)

            container.removeAllViewsInLayout()
            for (courseBundle in courses) {
                when {
                    noDetail -> ViewListCourseSimpleItemBinding.inflate(layoutInflater, container, false).apply {
                        ImageViewCompat.setImageTintList(ivListCourseColor, ColorStateList.valueOf(courseBundle.courseCellStyle.color))
                        tvListCourseName.text = courseBundle.courseItem.course.name
                        tvListCourseTime.text = getString(
                            R.string.course_simple_time, weekDayNumStrArray[courseBundle.courseItem.courseTime.weekDay - 1],
                            courseBundle.courseItem.courseTime.rawCoursesNumStr,
                            courseBundle.courseItem.courseTime.location
                        )
                    }
                    courseBundle.courseItem.detail != null -> ViewListCourseItemBinding.inflate(layoutInflater, container, false).apply {
                        ImageViewCompat.setImageTintList(ivListCourseColor, ColorStateList.valueOf(courseBundle.courseCellStyle.color))
                        tvListCourseName.text = courseBundle.courseItem.course.name
                        tvListCourseDetail.text =
                            ViewUtils.getCourseDataShowText("${courseBundle.courseItem.course.teacher}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${courseBundle.courseItem.courseTime.location}")
                        tvListCourseStartTime.text = DATE_FORMAT_HM.format(courseBundle.courseItem.detail.dateTimePeriod.startDateTime)
                    }
                    else -> null
                }?.apply {
                    root.setOnClickListener {
                        getViewModel().requestCourseDetail(courseBundle.courseItem, courseBundle.courseCellStyle)
                    }
                    container.addViewInLayout(root)
                }
            }
            container.refreshLayout()

            container.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
}