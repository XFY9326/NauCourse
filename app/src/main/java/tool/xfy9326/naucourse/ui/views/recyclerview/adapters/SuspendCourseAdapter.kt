package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.ViewGroup
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.databinding.ViewSuspendCourseDetailItemBinding
import tool.xfy9326.naucourse.databinding.ViewSuspendCourseItemBinding
import tool.xfy9326.naucourse.providers.beans.jwc.SuspendCourse
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.SuspendCourseViewHolder
import java.text.SimpleDateFormat
import java.util.*

class SuspendCourseAdapter(private val context: Context) :
    ListRecyclerAdapter<SuspendCourseViewHolder, SuspendCourse>(context, DifferItemCallback()) {
    companion object {
        private val DATE_FORMAT_YMD_CH = SimpleDateFormat(TimeConst.FORMAT_YMD_CH, Locale.CHINA)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SuspendCourseViewHolder(ViewSuspendCourseItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: SuspendCourseViewHolder, position: Int, element: SuspendCourse) {
        holder.apply {
            tvSuspendCourseName.text = element.name
            tvSuspendCourseClass.text = context.getString(R.string.course_class, element.teachClass)
            tvSuspendCourseTeacher.text = context.getString(R.string.course_teacher, element.teacher)
            layoutSuspendCourseDetail.removeAllViews()
            for (detail in element.detail) {
                layoutSuspendCourseDetail.addView(
                    ViewSuspendCourseDetailItemBinding.inflate(layoutInflater).apply {
                        tvSuspendCourseDetailType.text = detail.type
                        tvSuspendCourseDetailTime.text = context.getString(
                            R.string.suspend_course_time, DATE_FORMAT_YMD_CH.format(detail.date), detail.time
                        )
                        tvSuspendCourseDetailLocation.text = context.getString(R.string.course_location, detail.location)
                    }.root
                )
            }
        }
    }

    private class DifferItemCallback : SimpleDifferItemCallBack<SuspendCourse>() {
        override fun areContentsTheSame(oldItem: SuspendCourse, newItem: SuspendCourse): Boolean {
            return oldItem.name == newItem.name
        }
    }
}