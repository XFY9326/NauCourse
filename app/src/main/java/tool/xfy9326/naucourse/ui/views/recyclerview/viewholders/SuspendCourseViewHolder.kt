package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_suspend_course_item.view.*

class SuspendCourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvSuspendCourseName: MaterialTextView = view.tv_suspendCourseName
    val tvSuspendCourseClass: MaterialTextView = view.tv_suspendCourseClass
    val tvSuspendCourseTeacher: MaterialTextView = view.tv_suspendCourseTeacher
    val layoutSuspendCourseDetail: LinearLayoutCompat = view.layout_suspendCourseDetail
}