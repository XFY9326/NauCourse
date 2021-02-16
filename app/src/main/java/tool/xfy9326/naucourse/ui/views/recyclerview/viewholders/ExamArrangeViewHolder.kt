package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewExamItemBinding

class ExamArrangeViewHolder(binding: ViewExamItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvExamProperty: MaterialTextView = binding.tvExamProperty
    val tvExamTypeAndCredit: MaterialTextView = binding.tvExamTypeAndCredit
    val tvExamName: MaterialTextView = binding.tvExamName
    val tvExamTeachClass: MaterialTextView = binding.tvExamTeachClass
    val tvExamStartTime: MaterialTextView = binding.tvExamStartTime
    val tvExamEndTime: MaterialTextView = binding.tvExamEndTime
    val tvExamLocation: MaterialTextView = binding.tvExamLocation
    val tvExamCountDown: MaterialTextView = binding.tvExamCountDown
    val tvExamCountDownTimeUnit: MaterialTextView = binding.tvExamCountDownTimeUnit
    val layoutExamCountDown: LinearLayoutCompat = binding.layoutExamCountDown
}