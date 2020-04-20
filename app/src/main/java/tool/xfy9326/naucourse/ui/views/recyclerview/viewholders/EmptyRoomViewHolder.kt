package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_empty_room_item.view.*

class EmptyRoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvEmptyRoomName: MaterialTextView = view.tv_emptyRoomName
    val tvEmptyRoomSize: MaterialTextView = view.tv_emptyRoomSize
    val tvEmptyRoomType: MaterialTextView = view.tv_emptyRoomType
}