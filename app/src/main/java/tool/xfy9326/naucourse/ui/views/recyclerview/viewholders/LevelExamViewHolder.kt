package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_level_exam_item.view.*

class LevelExamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvLevelExamName: MaterialTextView = view.tv_levelExamName
    val tvLevelExamType: MaterialTextView = view.tv_levelExamType
    val tvLevelExamTerm: MaterialTextView = view.tv_levelExamTerm
    val tvLevelExamGrade1: MaterialTextView = view.tv_levelExamGrade1
    val tvLevelExamGrade2: MaterialTextView = view.tv_levelExamGrade2
    val tvLevelExamTicketNum: MaterialTextView = view.tv_levelExamTicketNum
    val tvLevelExamCertificateNum: MaterialTextView = view.tv_levelExamCertificateNum
    val tvLevelExamNote: MaterialTextView = view.tv_levelExamNote
}