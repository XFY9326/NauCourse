package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewCourseHistoryItemBinding

class CourseHistoryViewHolder(binding: ViewCourseHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvCourseHistoryName: MaterialTextView = binding.tvCourseHistoryName
    val tvCourseHistoryProperty: MaterialTextView = binding.tvCourseHistoryProperty
    val tvCourseHistoryTerm: MaterialTextView = binding.tvCourseHistoryTerm
    val tvCourseHistoryCredit: MaterialTextView = binding.tvCourseHistoryCredit
    val tvCourseHistoryWeight: MaterialTextView = binding.tvCourseHistoryWeight
    val tvCourseHistoryScore: MaterialTextView = binding.tvCourseHistoryScore
    val tvCourseHistoryType: MaterialTextView = binding.tvCourseHistoryType
    val tvCourseHistoryNote: MaterialTextView = binding.tvCourseHistoryNote
}