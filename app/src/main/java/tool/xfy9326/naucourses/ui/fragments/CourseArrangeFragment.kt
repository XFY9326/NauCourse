package tool.xfy9326.naucourses.ui.fragments

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
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseItem
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.ui.activities.MainDrawerActivity
import tool.xfy9326.naucourses.ui.dialogs.CourseDetailDialog
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.fragment.CourseArrangeViewModel
import tool.xfy9326.naucourses.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourses.utils.views.I18NUtils
import tool.xfy9326.naucourses.utils.views.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

class CourseArrangeFragment : DrawerToolbarFragment<CourseArrangeViewModel>() {
    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
        private val DATE_FORMAT_HM = SimpleDateFormat(Constants.Time.FORMAT_MD_HM, Locale.CHINA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateContentView(): Int = R.layout.fragment_course_arrange

    override fun onCreateViewModel(): CourseArrangeViewModel = ViewModelProvider(this)[CourseArrangeViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_general.apply {
        title = getString(R.string.course_arrange)
    }

    override fun onStart() {
        super.onStart()
        getViewModel().refreshArrangeCourses(false)
    }

    override fun initView(viewModel: CourseArrangeViewModel) {
        asl_todayCourse.setOnRefreshListener {
            viewModel.refreshArrangeCourses(true)
        }
        tv_todayCourseMore.setOnClickListener {
            (requireActivity() as MainDrawerActivity).showFragment(MainDrawerActivity.FragmentType.COURSE_TABLE)
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
            buildTodayCourseList(it)
        })
        viewModel.tomorrowCourses.observe(viewLifecycleOwner, Observer {
            buildTomorrowCourseList(it)
        })
        viewModel.notThisWeekCourse.observe(viewLifecycleOwner, Observer {
            buildNotThisWeekCourseList(it)
        })
        viewModel.nextCourseData.observe(viewLifecycleOwner, Observer {
            buildNextCourse(it)
        })
        viewModel.termDateData.observe(viewLifecycleOwner, Observer {
            buildTermDate(it)
        })
        viewModel.courseDetail.observeEvent(viewLifecycleOwner, Observer {
            CourseDetailDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(CourseDetailDialog.COURSE_DETAIL_DATA, it)
                }
            }.show(childFragmentManager, null)
        })
        App.instance.courseStyleTermUpdate.observeNotification(viewLifecycleOwner, {
            getViewModel().refreshArrangeCourses(false)
        }, CourseArrangeFragment::class.java.simpleName)
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
        } else {
            tv_nextCourseName.text = courseItem.course.name
            tv_nextCourseTime.text = DATE_FORMAT_HM.format(courseItem.dateTimePeriod.startDateTime) + "~" +
                    DATE_FORMAT_HM.format(courseItem.dateTimePeriod.endDateTime)
            tv_nextCourseDetail.text =
                ViewUtils.getCourseDataShowText("${courseItem.courseTime.location}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${courseItem.course.teacher}")

            layout_nextCourse.visibility = View.VISIBLE
            layout_nextCourseBreak.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildTodayCourseList(todayCourses: Array<Pair<CourseItem, CourseCellStyle>>) {
        if (todayCourses.isEmpty()) {
            layout_todayCourseContent.visibility = View.GONE
            tv_todayCourseEmpty.visibility = View.VISIBLE
        } else {
            val inflater = LayoutInflater.from(requireContext())

            layout_todayCourseContent.removeAllViewsInLayout()
            for (coursePair in todayCourses) {
                layout_todayCourseContent.addViewInLayout(inflater.inflate(R.layout.view_list_course_item, layout_todayCourseContent, false).apply {
                    ImageViewCompat.setImageTintList(this.iv_listCourseColor, ColorStateList.valueOf(coursePair.second.color))
                    this.tv_listCourseName.text = coursePair.first.course.name
                    this.tv_listCourseDetail.text =
                        ViewUtils.getCourseDataShowText("${coursePair.first.course.teacher}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${coursePair.first.courseTime.location}")
                    this.tv_listCourseStartTime.text = DATE_FORMAT_HM.format(coursePair.first.dateTimePeriod.startDateTime)
                    setOnClickListener {
                        getViewModel().requestCourseDetail(coursePair.first, coursePair.second)
                    }
                })
            }
            layout_todayCourseContent.refreshLayout()

            layout_todayCourseContent.visibility = View.VISIBLE
            tv_todayCourseEmpty.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildTomorrowCourseList(tomorrowCourses: Array<Pair<CourseItem, CourseCellStyle>>) {
        if (tomorrowCourses.isEmpty()) {
            layout_tomorrowCourseContent.visibility = View.GONE
            tv_tomorrowCourseEmpty.visibility = View.VISIBLE
        } else {
            val inflater = LayoutInflater.from(requireContext())

            layout_tomorrowCourseContent.removeAllViewsInLayout()
            for (coursePair in tomorrowCourses) {
                layout_tomorrowCourseContent.addViewInLayout(
                    inflater.inflate(
                        R.layout.view_list_course_item,
                        layout_todayCourseContent,
                        false
                    ).apply {
                        ImageViewCompat.setImageTintList(this.iv_listCourseColor, ColorStateList.valueOf(coursePair.second.color))
                        this.tv_listCourseName.text = coursePair.first.course.name
                        this.tv_listCourseDetail.text =
                            ViewUtils.getCourseDataShowText("${coursePair.first.course.teacher}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${coursePair.first.courseTime.location}")
                        this.tv_listCourseStartTime.text = DATE_FORMAT_HM.format(coursePair.first.dateTimePeriod.startDateTime)
                        setOnClickListener {
                            getViewModel().requestCourseDetail(coursePair.first, coursePair.second)
                        }
                    })
            }
            layout_tomorrowCourseContent.refreshLayout()

            layout_tomorrowCourseContent.visibility = View.VISIBLE
            tv_tomorrowCourseEmpty.visibility = View.GONE
        }
    }

    private fun buildNotThisWeekCourseList(notThisWeekCourses: Array<Triple<Course, CourseTime, CourseCellStyle>>) {
        if (notThisWeekCourses.isEmpty()) {
            layout_notThisWeekCourseContent.visibility = View.GONE
            tv_notThisWeekCourseEmpty.visibility = View.VISIBLE
        } else {
            val inflater = LayoutInflater.from(requireContext())
            val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)

            layout_notThisWeekCourseContent.removeAllViewsInLayout()
            for (coursePair in notThisWeekCourses) {
                layout_notThisWeekCourseContent.addViewInLayout(
                    inflater.inflate(
                        R.layout.view_list_course_simple_item,
                        layout_todayCourseContent,
                        false
                    ).apply {
                        ImageViewCompat.setImageTintList(this.iv_listCourseColor, ColorStateList.valueOf(coursePair.third.color))
                        this.tv_listCourseName.text = coursePair.first.name
                        this.tv_listCourseTime.text = getString(
                            R.string.course_simple_time, weekDayNumStrArray[coursePair.second.weekDay - 1],
                            coursePair.second.rawCoursesNumStr,
                            coursePair.second.location
                        )
                        setOnClickListener {
                            getViewModel().requestCourseDetail(coursePair.first, coursePair.third)
                        }
                    })
            }
            layout_notThisWeekCourseContent.refreshLayout()

            layout_notThisWeekCourseContent.visibility = View.VISIBLE
            tv_notThisWeekCourseEmpty.visibility = View.GONE
        }
    }
}