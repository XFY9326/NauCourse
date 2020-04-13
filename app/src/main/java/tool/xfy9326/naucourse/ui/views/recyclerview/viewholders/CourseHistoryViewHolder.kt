package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_course_history_item.view.*

class CourseHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvCourseHistoryName: MaterialTextView = view.tv_courseHistoryName
    val tvCourseHistoryProperty: MaterialTextView = view.tv_courseHistoryProperty
    val tvCourseHistoryTerm: MaterialTextView = view.tv_courseHistoryTerm
    val tvCourseHistoryCredit: MaterialTextView = view.tv_courseHistoryCredit
    val tvCourseHistoryWeight: MaterialTextView = view.tv_courseHistoryWeight
    val tvCourseHistoryScore: MaterialTextView = view.tv_courseHistoryScore
    val tvCourseHistoryType: MaterialTextView = view.tv_courseHistoryType
    val tvCourseHistoryNote: MaterialTextView = view.tv_courseHistoryNote
}