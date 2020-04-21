package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.view_suspend_course_detail_item.view.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.SuspendCourse
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.SuspendCourseViewHolder
import java.text.SimpleDateFormat
import java.util.*

class SuspendCourseAdapter(private val context: Context) :
    ListRecyclerAdapter<SuspendCourseViewHolder, SuspendCourse>(context, DifferItemCallback()) {
    companion object {
        private val DATE_FORMAT_YMD_CH = SimpleDateFormat(Constants.Time.FORMAT_YMD_CH, Locale.CHINA)
    }

    override fun onBindLayout(): Int = R.layout.view_suspend_course_item

    override fun onCreateViewHolder(view: View): SuspendCourseViewHolder = SuspendCourseViewHolder(view)

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: SuspendCourseViewHolder, position: Int, element: SuspendCourse) {
        holder.apply {
            tvSuspendCourseName.text = element.name
            tvSuspendCourseClass.text = context.getString(R.string.course_class, element.teachClass)
            tvSuspendCourseTeacher.text = context.getString(R.string.course_teacher, element.teacher)
            layoutSuspendCourseDetail.removeAllViews()
            for (detail in element.detail) {
                layoutSuspendCourseDetail.addView(layoutInflater.inflate(R.layout.view_suspend_course_detail_item, null).apply {
                    tv_suspendCourseDetailType.text = detail.type
                    tv_suspendCourseDetailTime.text = context.getString(
                        R.string.suspend_course_time, DATE_FORMAT_YMD_CH.format(detail.date), detail.time
                    )
                    tv_suspendCourseDetailLocation.text = context.getString(R.string.course_location, detail.location)
                })
            }
        }
    }

    private class DifferItemCallback : SimpleDifferItemCallBack<SuspendCourse>() {
        override fun areContentsTheSame(oldItem: SuspendCourse, newItem: SuspendCourse): Boolean {
            return oldItem.name == newItem.name
        }
    }
}