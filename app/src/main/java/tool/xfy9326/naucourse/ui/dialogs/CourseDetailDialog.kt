package tool.xfy9326.naucourse.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.dialog_course_detail.view.*
import kotlinx.android.synthetic.main.view_course_detail_item.view.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseDetail
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.ColorUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

class CourseDetailDialog : DialogFragment() {
    private lateinit var courseDetail: CourseDetail
    private var isMoreInfoExpanded = SettingsPref.ExpandCourseDetailInDefault

    companion object {
        private const val COURSE_DETAIL_DATA = "COURSE_DETAIL_DATA"
        private const val VIEW_EXPANDED = "VIEW_EXPANDED"

        private const val CONTENT_WIDTH_PERCENT = 0.75

        private val DATE_FORMAT_MD_HM_CH = SimpleDateFormat(TimeConst.FORMAT_MD_HM_CH, Locale.CHINA)
        private val DATE_FORMAT_HM = SimpleDateFormat(TimeConst.FORMAT_HM, Locale.CHINA)

        fun showDialog(fragmentManager: FragmentManager, courseDetail: CourseDetail) {
            CourseDetailDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(COURSE_DETAIL_DATA, courseDetail)
                }
            }.show(fragmentManager, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            courseDetail = getSerializable(COURSE_DETAIL_DATA) as CourseDetail
        }
        savedInstanceState?.let {
            isMoreInfoExpanded = it.getBoolean(VIEW_EXPANDED)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(VIEW_EXPANDED, isMoreInfoExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(R.layout.dialog_course_detail, container, false).apply {
            val view = this
            layout_courseTitle.setBackgroundColor(courseDetail.courseCellStyle.color)
            tv_courseName.text = courseDetail.course.name

            if (courseDetail.timeDetail != null) {
                val timePeriod = TimeUtils.getCourseDateTimePeriod(
                    courseDetail.termDate.startDate, courseDetail.timeDetail!!.weekNum, courseDetail.timeDetail!!.weekDayNum,
                    courseDetail.timeDetail!!.timePeriod
                )
                tv_courseCellTime.text =
                    getString(
                        R.string.time_duration,
                        DATE_FORMAT_MD_HM_CH.format(timePeriod.startDateTime),
                        DATE_FORMAT_HM.format(timePeriod.endDateTime)
                    )
                if (courseDetail.timeDetail!!.courseLocation.isNotEmpty() &&
                    courseDetail.timeDetail!!.courseLocation.isNotBlank() &&
                    courseDetail.timeDetail!!.courseLocation != getString(R.string.no_data)
                ) {
                    tv_courseCellLocation.text = courseDetail.timeDetail!!.courseLocation
                } else {
                    tv_courseCellLocation.visibility = View.GONE
                }
            } else {
                tv_courseCellTime.visibility = View.GONE
                tv_courseCellLocation.visibility = View.GONE
                layout_courseTitle.apply {
                    layoutParams = LinearLayoutCompat.LayoutParams(layoutParams).apply {
                        setPadding(
                            paddingLeft, paddingTop + resources.getDimensionPixelSize(R.dimen.course_detail_layout_height_make_up),
                            paddingRight, paddingBottom
                        )
                    }
                }
            }

            val colorDark = ContextCompat.getColor(requireContext(), R.color.colorCourseTextDark)
            val colorLight = ContextCompat.getColor(requireContext(), R.color.colorCourseTextLight)
            if (ColorUtils.isLightColor(courseDetail.courseCellStyle.color)) {
                tv_courseName.setTextColor(colorDark)
                tv_courseCellTime.setTextColor(colorDark)
                tv_courseCellLocation.setTextColor(colorDark)
            } else {
                tv_courseName.setTextColor(colorLight)
                tv_courseCellTime.setTextColor(colorLight)
                tv_courseCellLocation.setTextColor(colorLight)
            }

            tv_courseID.text = getString(R.string.course_id, courseDetail.course.id)
            tv_teacher.text = getString(R.string.course_teacher, ViewUtils.getCourseDataShowText(courseDetail.course.teacher))
            tv_class.text = getString(
                R.string.course_class, ViewUtils.getCourseDataShowText(
                    if (courseDetail.course.courseClass == null || courseDetail.course.courseClass == courseDetail.course.teachClass) {
                        courseDetail.course.teachClass
                    } else {
                        "${courseDetail.course.teachClass} ${courseDetail.course.courseClass}"
                    }
                )
            )
            tv_credit.text = getString(R.string.course_credit, courseDetail.course.credit)
            tv_courseType.text = getString(
                R.string.course_type, ViewUtils.getCourseDataShowText(
                    if (courseDetail.course.property == null) {
                        courseDetail.course.type
                    } else {
                        "${courseDetail.course.type} ${courseDetail.course.property}"
                    }
                )
            )

            btn_loadMoreCourseInfo.setOnClickListener {
                (it as AppCompatImageButton).apply {
                    if (isMoreInfoExpanded) {
                        isMoreInfoExpanded = false
                        setImageResource(R.drawable.ic_load_more)
                        loadLess(view)
                    } else {
                        isMoreInfoExpanded = true
                        setImageResource(R.drawable.ic_load_less)
                        loadMore(view, inflater)
                    }
                }
            }

            if (isMoreInfoExpanded) {
                btn_loadMoreCourseInfo.setImageResource(R.drawable.ic_load_less)
                loadMore(this, inflater)
            }
        }
    }

    private fun loadMore(contentView: View, inflater: LayoutInflater) {
        val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)
        val showList = courseDetail.course.timeSet.toList().sortedWith { o1, o2 ->
            o1.compareTo(o2)
        }
        for ((i, courseTime) in showList.withIndex()) {
            contentView.layout_moreCourseInfo.addViewInLayout(
                inflater.inflate(
                    R.layout.view_course_detail_item,
                    contentView.layout_moreCourseInfo,
                    false
                ).apply {
                    tv_courseLocation.text = getString(R.string.course_location, ViewUtils.getCourseDataShowText(courseTime.location))
                    tv_courseTime.text = getString(
                        R.string.course_time,
                        courseTime.rawWeeksStr,
                        weekDayNumStrArray[courseTime.weekDay - 1],
                        courseTime.rawCoursesNumStr
                    )
                })
            if (i != courseDetail.course.timeSet.size - 1) {
                contentView.layout_moreCourseInfo.addViewInLayout(
                    inflater.inflate(
                        R.layout.view_divider,
                        contentView.layout_moreCourseInfo,
                        false
                    )
                )
            }
        }
        contentView.layout_moreCourseInfo.refreshLayout()
        contentView.layout_moreCourseInfo.visibility = View.VISIBLE
    }

    private fun loadLess(contentView: View) {
        contentView.layout_moreCourseInfo.visibility = View.GONE
        contentView.layout_moreCourseInfo.removeAllViews()
    }

    override fun onStart() {
        super.onStart()
        DialogUtils.applyBackgroundAndWidth(requireContext(), dialog, CONTENT_WIDTH_PERCENT)
    }
}