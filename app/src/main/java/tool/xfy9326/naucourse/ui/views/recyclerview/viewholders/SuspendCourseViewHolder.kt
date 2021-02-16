package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewSuspendCourseItemBinding

class SuspendCourseViewHolder(binding: ViewSuspendCourseItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvSuspendCourseName: MaterialTextView = binding.tvSuspendCourseName
    val tvSuspendCourseClass: MaterialTextView = binding.tvSuspendCourseClass
    val tvSuspendCourseTeacher: MaterialTextView = binding.tvSuspendCourseTeacher
    val layoutSuspendCourseDetail: LinearLayoutCompat = binding.layoutSuspendCourseDetail
}