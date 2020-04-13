package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_course_score_item.view.*

class CourseScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvCourseScoreCourseType: MaterialTextView = view.tv_courseScoreCourseType
    val tvCourseScoreCourseProperty: MaterialTextView = view.tv_courseScoreCourseProperty
    val tvCourseScoreName: MaterialTextView = view.tv_courseScoreName
    val tvCourseScoreClass: MaterialTextView = view.tv_courseScoreClass
    val tvCourseScoreOrdinary: MaterialTextView = view.tv_courseScoreOrdinary
    val tvCourseScoreMidTerm: MaterialTextView = view.tv_courseScoreMidTerm
    val tvCourseScoreFinalTerm: MaterialTextView = view.tv_courseScoreFinalTerm
    val tvCourseScoreCredit: MaterialTextView = view.tv_courseScoreCredit
    val tvCourseScoreOverAll: MaterialTextView = view.tv_courseScoreOverAll
}