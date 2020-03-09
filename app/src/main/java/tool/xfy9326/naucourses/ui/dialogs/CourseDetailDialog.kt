package tool.xfy9326.naucourses.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_course_detail.view.*
import kotlinx.android.synthetic.main.view_course_detail_item.view.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseDetail
import tool.xfy9326.naucourses.utils.compute.TimeUtils
import tool.xfy9326.naucourses.utils.views.ColorUtils
import java.text.SimpleDateFormat
import java.util.*

class CourseDetailDialog : DialogFragment() {
    private lateinit var courseDetail: CourseDetail
    private var isMoreInfoExpanded = false

    companion object {
        const val COURSE_DETAIL_DATA = "COURSE_DETAIL_DATA"
        private const val VIEW_EXPANDED = "VIEW_EXPANDED"

        private const val CONTENT_WIDTH_PERCENT = 0.75

        private val DATE_FORMAT_MD_HM_CH = SimpleDateFormat(Constants.Time.FORMAT_MD_HM_CH, Locale.CHINA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            courseDetail = getSerializable(COURSE_DETAIL_DATA) as CourseDetail
        }
        if (savedInstanceState != null) {
            isMoreInfoExpanded = savedInstanceState.getBoolean(VIEW_EXPANDED)
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
                tv_courseCellTime.text = DATE_FORMAT_MD_HM_CH.format(timePeriod.startDateTime)
                tv_courseCellLocation.text = courseDetail.timeDetail!!.courseLocation
            } else {
                tv_courseCellTime.visibility = View.GONE
                tv_courseCellLocation.visibility = View.GONE
            }

            val colorDark = activity?.getColor(R.color.colorCourseTextDark)!!
            val colorLight = activity?.getColor(R.color.colorCourseTextLight)!!
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
            tv_teacher.text = getString(R.string.course_teacher, courseDetail.course.teacher)
            tv_class.text = getString(
                R.string.course_class, if (courseDetail.course.courseClass == null) {
                    courseDetail.course.teachClass
                } else {
                    "${courseDetail.course.teachClass} ${courseDetail.course.courseClass}"
                }
            )
            tv_credit.text = getString(R.string.course_credit, courseDetail.course.credit)
            tv_courseType.text = getString(
                R.string.course_type, if (courseDetail.course.property == null) {
                    courseDetail.course.type
                } else {
                    "${courseDetail.course.type} ${courseDetail.course.property}"
                }
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
        for ((i, courseTime) in courseDetail.course.timeSet.withIndex()) {
            contentView.layout_moreCourseInfo.addViewInLayout(
                inflater.inflate(
                    R.layout.view_course_detail_item,
                    contentView.layout_moreCourseInfo,
                    false
                ).apply {
                    tv_courseLocation.text = getString(R.string.course_location, courseTime.location)
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
    }

    private fun loadLess(contentView: View) = contentView.layout_moreCourseInfo.removeAllViews()

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val displayMetrics = activity?.resources?.displayMetrics!!
            window?.apply {
                setLayout((displayMetrics.widthPixels * CONTENT_WIDTH_PERCENT).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(activity?.getDrawable(R.drawable.bg_dialog))
            }
        }
    }
}