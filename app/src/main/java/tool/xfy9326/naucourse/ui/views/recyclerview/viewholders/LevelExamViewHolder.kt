package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewLevelExamItemBinding

class LevelExamViewHolder(binding: ViewLevelExamItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvLevelExamName: MaterialTextView = binding.tvLevelExamName
    val tvLevelExamType: MaterialTextView = binding.tvLevelExamType
    val tvLevelExamTerm: MaterialTextView = binding.tvLevelExamTerm
    val tvLevelExamGrade1: MaterialTextView = binding.tvLevelExamGrade1
    val tvLevelExamGrade2: MaterialTextView = binding.tvLevelExamGrade2
    val tvLevelExamTicketNum: MaterialTextView = binding.tvLevelExamTicketNum
    val tvLevelExamCertificateNum: MaterialTextView = binding.tvLevelExamCertificateNum
    val tvLevelExamNote: MaterialTextView = binding.tvLevelExamNote
}