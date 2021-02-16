package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewEmptyRoomItemBinding

class EmptyRoomViewHolder(binding: ViewEmptyRoomItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvEmptyRoomName: MaterialTextView = binding.tvEmptyRoomName
    val tvEmptyRoomSize: MaterialTextView = binding.tvEmptyRoomSize
    val tvEmptyRoomType: MaterialTextView = binding.tvEmptyRoomType
}