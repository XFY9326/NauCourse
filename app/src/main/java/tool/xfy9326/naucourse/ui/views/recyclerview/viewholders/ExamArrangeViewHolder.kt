package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_exam_item.view.*

class ExamArrangeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvExamProperty: MaterialTextView = view.tv_examProperty
    val tvExamTypeAndCredit: MaterialTextView = view.tv_examTypeAndCredit
    val tvExamName: MaterialTextView = view.tv_examName
    val tvExamTeachClass: MaterialTextView = view.tv_examTeachClass
    val tvExamStartTime: MaterialTextView = view.tv_examStartTime
    val tvExamEndTime: MaterialTextView = view.tv_examEndTime
    val tvExamLocation: MaterialTextView = view.tv_examLocation
    val tvExamCountDown: MaterialTextView = view.tv_examCountDown
    val tvExamCountDownTimeUnit: MaterialTextView = view.tv_examCountDownTimeUnit
    val layoutExamCountDown: LinearLayoutCompat = view.layout_examCountDown
}