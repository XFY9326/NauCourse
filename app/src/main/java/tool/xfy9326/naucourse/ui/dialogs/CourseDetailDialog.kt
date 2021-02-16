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
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseDetail
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.databinding.DialogCourseDetailBinding
import tool.xfy9326.naucourse.databinding.ViewCourseDetailItemBinding
import tool.xfy9326.naucourse.databinding.ViewDividerBinding
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
    private lateinit var binding: DialogCourseDetailBinding

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        binding = DialogCourseDetailBinding.inflate(layoutInflater, container, false).apply {
            layoutCourseTitle.setBackgroundColor(courseDetail.courseCellStyle.color)
            tvCourseName.text = courseDetail.course.name

            if (courseDetail.timeDetail != null) {
                val timePeriod = TimeUtils.getCourseDateTimePeriod(
                    courseDetail.termDate.startDate, courseDetail.timeDetail!!.weekNum, courseDetail.timeDetail!!.weekDayNum,
                    courseDetail.timeDetail!!.timePeriod
                )
                tvCourseCellTime.text =
                    getString(
                        R.string.time_duration,
                        DATE_FORMAT_MD_HM_CH.format(timePeriod.startDateTime),
                        DATE_FORMAT_HM.format(timePeriod.endDateTime)
                    )
                if (courseDetail.timeDetail!!.courseLocation.isNotEmpty() &&
                    courseDetail.timeDetail!!.courseLocation.isNotBlank() &&
                    courseDetail.timeDetail!!.courseLocation != getString(R.string.no_data)
                ) {
                    tvCourseCellLocation.text = courseDetail.timeDetail!!.courseLocation
                } else {
                    tvCourseCellLocation.visibility = View.GONE
                }
            } else {
                tvCourseCellTime.visibility = View.GONE
                tvCourseCellLocation.visibility = View.GONE
                layoutCourseTitle.apply {
                    layoutParams = LinearLayoutCompat.LayoutParams(layoutParams).apply {
                        setPadding(
                            paddingLeft, paddingTop + resources.getDimensionPixelSize(R.dimen.course_detail_layout_height_make_up),
                            paddingRight, paddingBottom
                        )
                    }
                }
            }

            val textColor = if (ColorUtils.isLightColor(courseDetail.courseCellStyle.color)) {
                ContextCompat.getColor(requireContext(), R.color.colorCourseTextDark)
            } else {
                ContextCompat.getColor(requireContext(), R.color.colorCourseTextLight)
            }
            tvCourseName.setTextColor(textColor)
            tvCourseCellTime.setTextColor(textColor)
            tvCourseCellLocation.setTextColor(textColor)

            tvCourseID.text = getString(R.string.course_id, courseDetail.course.id)
            tvTeacher.text = getString(R.string.course_teacher, ViewUtils.getCourseDataShowText(courseDetail.course.teacher))
            tvClass.text = getString(
                R.string.course_class, ViewUtils.getCourseDataShowText(
                    if (courseDetail.course.courseClass == null || courseDetail.course.courseClass == courseDetail.course.teachClass) {
                        courseDetail.course.teachClass
                    } else {
                        "${courseDetail.course.teachClass} ${courseDetail.course.courseClass}"
                    }
                )
            )
            tvCredit.text = getString(R.string.course_credit, courseDetail.course.credit)
            tvCourseType.text = getString(
                R.string.course_type, ViewUtils.getCourseDataShowText(
                    if (courseDetail.course.property == null) {
                        courseDetail.course.type
                    } else {
                        "${courseDetail.course.type} ${courseDetail.course.property}"
                    }
                )
            )

            btnLoadMoreCourseInfo.setOnClickListener {
                (it as AppCompatImageButton).apply {
                    if (isMoreInfoExpanded) {
                        isMoreInfoExpanded = false
                        setImageResource(R.drawable.ic_load_more)
                        loadLess()
                    } else {
                        isMoreInfoExpanded = true
                        setImageResource(R.drawable.ic_load_less)
                        loadMore()
                    }
                }
            }

            if (isMoreInfoExpanded) {
                btnLoadMoreCourseInfo.setImageResource(R.drawable.ic_load_less)
                loadMore()
            }
        }
        return binding.root
    }

    private fun loadMore() {
        val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)
        val showList = courseDetail.course.timeSet.toList().sortedWith { o1, o2 ->
            o1.compareTo(o2)
        }
        for ((i, courseTime) in showList.withIndex()) {
            binding.layoutMoreCourseInfo.addViewInLayout(
                ViewCourseDetailItemBinding.inflate(layoutInflater, binding.layoutMoreCourseInfo, false).apply {
                    tvCourseLocation.text = getString(R.string.course_location, ViewUtils.getCourseDataShowText(courseTime.location))
                    tvCourseTime.text = getString(
                        R.string.course_time,
                        courseTime.rawWeeksStr,
                        weekDayNumStrArray[courseTime.weekDay - 1],
                        courseTime.rawCoursesNumStr
                    )
                }.root
            )
            if (i != courseDetail.course.timeSet.size - 1) {
                binding.layoutMoreCourseInfo.addViewInLayout(ViewDividerBinding.inflate(layoutInflater, binding.layoutMoreCourseInfo, false).root)
            }
        }
        binding.layoutMoreCourseInfo.refreshLayout()
        binding.layoutMoreCourseInfo.visibility = View.VISIBLE
    }

    private fun loadLess() {
        binding.layoutMoreCourseInfo.visibility = View.GONE
        binding.layoutMoreCourseInfo.removeAllViews()
    }

    override fun onStart() {
        super.onStart()
        DialogUtils.applyBackgroundAndWidth(requireContext(), dialog, CONTENT_WIDTH_PERCENT)
    }
}