package tool.xfy9326.naucourse.ui.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_course_arrange.*
import kotlinx.android.synthetic.main.view_card_next_course.*
import kotlinx.android.synthetic.main.view_card_not_this_week_course.*
import kotlinx.android.synthetic.main.view_card_today_course.*
import kotlinx.android.synthetic.main.view_card_tomorrow_course.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import kotlinx.android.synthetic.main.view_list_course_item.view.*
import kotlinx.android.synthetic.main.view_list_course_item.view.iv_listCourseColor
import kotlinx.android.synthetic.main.view_list_course_item.view.tv_listCourseName
import kotlinx.android.synthetic.main.view_list_course_simple_item.view.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseBundle
import tool.xfy9326.naucourse.beans.CourseItem
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.ui.activities.MainDrawerActivity
import tool.xfy9326.naucourse.ui.dialogs.CourseDetailDialog
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.fragment.CourseArrangeViewModel
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedLinearLayout
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

class CourseArrangeFragment : DrawerToolbarFragment<CourseArrangeViewModel>() {
    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
        private val DATE_FORMAT_HM = SimpleDateFormat(Constants.Time.FORMAT_HM, Locale.CHINA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateContentView(): Int = R.layout.fragment_course_arrange

    override fun onCreateViewModel(): CourseArrangeViewModel = ViewModelProvider(this)[CourseArrangeViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_general.apply {
        title = getString(R.string.course_arrange)
    }

    override fun onStart() {
        if (SettingsPref.AutoUpdateCourseArrange) {
            getViewModel().refreshArrangeCourses(false)
        }
        super.onStart()
    }

    override fun initView(viewModel: CourseArrangeViewModel) {
        asl_todayCourse.setOnRefreshListener {
            viewModel.refreshArrangeCourses(true)
        }
        tv_todayCourseMore.setOnClickListener {
            (requireActivity() as MainDrawerActivity).showFragment(MainDrawerActivity.FragmentType.COURSE_TABLE)
        }
        if (!SettingsPref.AutoUpdateCourseArrange) {
            getViewModel().refreshArrangeCourses(false)
        }
    }

    override fun bindViewModel(viewModel: CourseArrangeViewModel) {
        viewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
            asl_todayCourse.post {
                asl_todayCourse.isRefreshing = it
            }
        })
        viewModel.notifyMsg.observeEvent(viewLifecycleOwner, Observer {
            showSnackBar(layout_todayCourse, I18NUtils.getTodayCourseNotifyTypeResId(it))
        })
        viewModel.todayCourses.observe(viewLifecycleOwner, Observer {
            buildListCourseItem(layout_todayCourseContent, tv_todayCourseEmpty, it)
        })
        viewModel.tomorrowCourses.observe(viewLifecycleOwner, Observer {
            buildListCourseItem(layout_tomorrowCourseContent, tv_tomorrowCourseEmpty, it)
        })
        viewModel.notThisWeekCourse.observe(viewLifecycleOwner, Observer {
            buildListCourseItem(layout_notThisWeekCourseContent, tv_notThisWeekCourseEmpty, it, true)
        })
        viewModel.nextCourseData.observe(viewLifecycleOwner, Observer {
            buildNextCourse(it)
        })
        viewModel.nextCourseBundle.observeEvent(viewLifecycleOwner, Observer {
            if (it == null) {
                AppWidgetUtils.refreshNextCourseWidget(requireContext())
            } else {
                AppWidgetUtils.updateNextCourseWidget(requireContext(), it)
            }
        })
        viewModel.termDateData.observe(viewLifecycleOwner, Observer {
            buildTermDate(it)
        })
        viewModel.courseDetail.observeEvent(viewLifecycleOwner, Observer {
            CourseDetailDialog.showDialog(childFragmentManager, it)
        })
        val notifyObserver = {
            getViewModel().refreshArrangeCourses(showAttention = false, updateNextCourseWidget = false)
            viewModel.nextCourseBundle.postEventValue(null)
            IntentUtils.refreshNextCourseAlarmData(requireContext())
        }
        NotifyBus[NotifyBus.Type.COURSE_ASYNC_UPDATE].observeNotification(
            viewLifecycleOwner,
            notifyObserver,
            CourseArrangeFragment::class.java.simpleName
        )
        NotifyBus[NotifyBus.Type.COURSE_STYLE_TERM_UPDATE].observeNotification(
            viewLifecycleOwner,
            notifyObserver,
            CourseArrangeFragment::class.java.simpleName
        )
    }

    private fun buildTermDate(termDate: TermDate?) {
        if (termDate == null) {
            tv_todayCourseDate.text = DATE_FORMAT_YMD.format(Date())

            tv_todayCourseTerm.visibility = View.GONE
            tv_todayCourseWeekNum.visibility = View.GONE
            tv_todayCourseDate.visibility = View.VISIBLE
        } else {
            val term = termDate.getTerm()
            tv_todayCourseTerm.text = getString(R.string.term, term.startYear, term.endYear, term.termNum)
            if (termDate.inVacation) {
                tv_todayCourseWeekNum.text = getString(R.string.in_vacation)
            } else {
                tv_todayCourseWeekNum.text = getString(R.string.week_num, termDate.currentWeekNum)
            }
            tv_todayCourseTerm.visibility = View.VISIBLE
            tv_todayCourseWeekNum.visibility = View.VISIBLE
            tv_todayCourseDate.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildNextCourse(courseItem: CourseItem?) {
        if (courseItem == null) {
            layout_nextCourse.visibility = View.GONE
            layout_nextCourseBreak.visibility = View.VISIBLE
        } else if (courseItem.detail != null) {
            tv_nextCourseName.text = courseItem.course.name
            tv_nextCourseTime.text = DATE_FORMAT_HM.format(courseItem.detail.dateTimePeriod.startDateTime) + "~" +
                    DATE_FORMAT_HM.format(courseItem.detail.dateTimePeriod.endDateTime)
            tv_nextCourseDetail.text =
                ViewUtils.getCourseDataShowText("${courseItem.courseTime.location}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${courseItem.course.teacher}")

            layout_nextCourse.visibility = View.VISIBLE
            layout_nextCourseBreak.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildListCourseItem(container: AdvancedLinearLayout, emptyView: View, courses: Array<CourseBundle>, noDetail: Boolean = false) {
        if (courses.isEmpty()) {
            container.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            val inflater = LayoutInflater.from(requireContext())
            val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)

            container.removeAllViewsInLayout()
            for (courseBundle in courses) {
                val view = when {
                    noDetail -> inflater.inflate(R.layout.view_list_course_simple_item, container, false).apply {
                        this.tv_listCourseTime.text = getString(
                            R.string.course_simple_time, weekDayNumStrArray[courseBundle.courseItem.courseTime.weekDay - 1],
                            courseBundle.courseItem.courseTime.rawCoursesNumStr,
                            courseBundle.courseItem.courseTime.location
                        )
                    }
                    courseBundle.courseItem.detail != null -> inflater.inflate(R.layout.view_list_course_item, container, false).apply {
                        this.tv_listCourseDetail.text =
                            ViewUtils.getCourseDataShowText("${courseBundle.courseItem.course.teacher}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${courseBundle.courseItem.courseTime.location}")
                        this.tv_listCourseStartTime.text = DATE_FORMAT_HM.format(courseBundle.courseItem.detail.dateTimePeriod.startDateTime)
                    }
                    else -> null
                }
                view?.let {
                    ImageViewCompat.setImageTintList(it.iv_listCourseColor, ColorStateList.valueOf(courseBundle.courseCellStyle.color))
                    it.tv_listCourseName.text = courseBundle.courseItem.course.name
                    it.setOnClickListener {
                        getViewModel().requestCourseDetail(courseBundle.courseItem, courseBundle.courseCellStyle)
                    }
                    container.addViewInLayout(it)
                }
            }
            container.refreshLayout()

            container.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
}