package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewCourseScoreItemBinding

class CourseScoreViewHolder(binding: ViewCourseScoreItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvCourseScoreCourseType: MaterialTextView = binding.tvCourseScoreCourseType
    val tvCourseScoreCourseProperty: MaterialTextView = binding.tvCourseScoreCourseProperty
    val tvCourseScoreName: MaterialTextView = binding.tvCourseScoreName
    val tvCourseScoreClass: MaterialTextView = binding.tvCourseScoreClass
    val tvCourseScoreOrdinary: MaterialTextView = binding.tvCourseScoreOrdinary
    val tvCourseScoreMidTerm: MaterialTextView = binding.tvCourseScoreMidTerm
    val tvCourseScoreFinalTerm: MaterialTextView = binding.tvCourseScoreFinalTerm
    val tvCourseScoreCredit: MaterialTextView = binding.tvCourseScoreCredit
    val tvCourseScoreOverAll: MaterialTextView = binding.tvCourseScoreOverAll
}