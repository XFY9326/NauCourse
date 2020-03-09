package tool.xfy9326.naucourses.ui.views.recyclerview.viewholders

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_course_manage_item.view.*

class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val layoutCourseManageItem: LinearLayoutCompat = view.layout_courseManageItem
    val layoutCourseManageColor: FrameLayout = view.layout_courseManageColor
    val ivCourseManageColor: AppCompatImageView = view.iv_courseManageColor
    val tvCourseManageName: MaterialTextView = view.tv_courseManageName
    val tvCourseManageDetail: MaterialTextView = view.tv_courseManageDetail
}