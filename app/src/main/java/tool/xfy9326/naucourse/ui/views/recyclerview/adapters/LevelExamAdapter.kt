package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.View
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.LevelExamViewHolder
import java.lang.ref.WeakReference

class LevelExamAdapter(context: Context) : ListRecyclerAdapter<LevelExamViewHolder, LevelExam>(context) {
    private val weakContext = WeakReference(context)

    override fun onBindLayout(): Int = R.layout.view_level_exam_item

    override fun onCreateViewHolder(view: View): LevelExamViewHolder = LevelExamViewHolder(view)

    override fun onBindViewHolder(holder: LevelExamViewHolder, position: Int, element: LevelExam) {
        holder.apply {
            tvLevelExamName.text = element.name
            tvLevelExamType.text = element.type
            tvLevelExamTerm.text = element.term.toString()
            tvLevelExamGrade1.text = weakContext.get()?.getString(R.string.grade_1, element.grade1 ?: Constants.EMPTY)
            tvLevelExamGrade2.text = weakContext.get()?.getString(R.string.grade_2, element.grade2)

            if (element.ticketNum.isEmpty() || element.ticketNum.isBlank()) {
                tvLevelExamTicketNum.visibility = View.GONE
            } else {
                tvLevelExamTicketNum.text = weakContext.get()?.getString(R.string.ticket_num, element.ticketNum)
                tvLevelExamTicketNum.visibility = View.VISIBLE
            }

            if (element.certificateNum.isEmpty() || element.certificateNum.isBlank()) {
                tvLevelExamCertificateNum.visibility = View.GONE
            } else {
                tvLevelExamCertificateNum.text = weakContext.get()?.getString(R.string.certificate_num, element.certificateNum)
                tvLevelExamCertificateNum.visibility = View.VISIBLE
            }

            if (element.notes.isEmpty() || element.notes.isBlank()) {
                tvLevelExamNote.visibility = View.GONE
            } else {
                tvLevelExamNote.text = weakContext.get()?.getString(R.string.notes, element.notes)
                tvLevelExamNote.visibility = View.VISIBLE
            }
        }
    }
}