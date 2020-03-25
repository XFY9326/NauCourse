package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_course_manage_item.view.*
import tool.xfy9326.naucourse.ui.views.recyclerview.SwipeItemViewHolder

class CourseViewHolder(view: View) : SwipeItemViewHolder(view) {
    override val foregroundSwipeView: ViewGroup = view.layout_courseManageItemForeground
    override val backgroundShowSwipeView: ViewGroup = view.layout_courseManageItemSwipeBackground
    override val imageViewSwipeIcon: AppCompatImageView = view.iv_courseManageItemDeleteIcon

    val layoutCourseManageItem: LinearLayoutCompat = view.layout_courseManageItemForeground
    val layoutCourseManageColor: FrameLayout = view.layout_courseManageColor
    val ivCourseManageColor: AppCompatImageView = view.iv_courseManageColor
    val tvCourseManageName: MaterialTextView = view.tv_courseManageName
    val tvCourseManageDetail: MaterialTextView = view.tv_courseManageDetail
}