package tool.xfy9326.naucourse.ui.views.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract val foregroundSwipeView: ViewGroup
    abstract val backgroundShowSwipeView: ViewGroup
    abstract val imageViewSwipeIcon: AppCompatImageView
}