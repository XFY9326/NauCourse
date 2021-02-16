package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewCourseManageItemBinding
import tool.xfy9326.naucourse.ui.views.recyclerview.SwipeItemViewHolder

class CourseViewHolder(binding: ViewCourseManageItemBinding) : SwipeItemViewHolder(binding.root) {
    override val foregroundSwipeView: ViewGroup = binding.layoutCourseManageItemForeground
    override val backgroundShowSwipeView: ViewGroup = binding.layoutCourseManageItemSwipeBackground
    override val imageViewSwipeIcon: AppCompatImageView = binding.ivCourseManageItemDeleteIcon

    val layoutCourseManageItem: LinearLayoutCompat = binding.layoutCourseManageItemForeground
    val layoutCourseManageColor: FrameLayout = binding.layoutCourseManageColor
    val ivCourseManageColor: AppCompatImageView = binding.ivCourseManageColor
    val tvCourseManageName: MaterialTextView = binding.tvCourseManageName
    val tvCourseManageDetail: MaterialTextView = binding.tvCourseManageDetail
}