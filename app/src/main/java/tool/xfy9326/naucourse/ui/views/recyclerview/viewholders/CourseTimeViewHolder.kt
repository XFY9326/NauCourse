package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewCourseTimeItemBinding

class CourseTimeViewHolder(binding: ViewCourseTimeItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val cvCourseEditTime: CardView = binding.cvCourseEditTime
    val layoutCourseTimeDelete: FrameLayout = binding.layoutCourseTimeDelete
    val tvCourseTimeWeeks: MaterialTextView = binding.tvCourseTimeWeeks
    val tvCourseTimeCourses: MaterialTextView = binding.tvCourseTimeCourses
    val tvCourseTimeLocation: MaterialTextView = binding.tvCourseTimeLocation
}