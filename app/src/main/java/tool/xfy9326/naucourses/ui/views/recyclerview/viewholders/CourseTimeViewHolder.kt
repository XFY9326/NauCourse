package tool.xfy9326.naucourses.ui.views.recyclerview.viewholders

import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_course_time_item.view.*

class CourseTimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val cvCourseEditTime: CardView = view.cv_courseEditTime
    val layoutCourseTimeDelete: FrameLayout = view.layout_courseTimeDelete
    val tvCourseTimeWeeks: MaterialTextView = view.tv_courseTimeWeeks
    val tvCourseTimeCourses: MaterialTextView = view.tv_courseTimeCourses
    val tvCourseTimeLocation: MaterialTextView = view.tv_courseTimeLocation
}