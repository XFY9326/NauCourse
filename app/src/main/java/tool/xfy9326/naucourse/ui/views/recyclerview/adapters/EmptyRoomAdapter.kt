package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.ViewGroup
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.ViewEmptyRoomItemBinding
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomSearchResult
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.EmptyRoomViewHolder

class EmptyRoomAdapter(private val context: Context) :
    ListRecyclerAdapter<EmptyRoomViewHolder, EmptyRoomSearchResult>(context, DifferItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EmptyRoomViewHolder(ViewEmptyRoomItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: EmptyRoomViewHolder, position: Int, element: EmptyRoomSearchResult) {
        holder.apply {
            tvEmptyRoomName.text = context.getString(R.string.empty_room_name, element.RoomName)
            tvEmptyRoomSize.text = context.getString(R.string.empty_room_size, element.RoomTotalSeatNum)
            tvEmptyRoomType.text = context.getString(R.string.empty_room_type, element.RoomTypeName)
        }
    }

    private class DifferItemCallback : SimpleDifferItemCallBack<EmptyRoomSearchResult>() {
        override fun areContentsTheSame(oldItem: EmptyRoomSearchResult, newItem: EmptyRoomSearchResult): Boolean {
            return oldItem.RoomID == newItem.RoomID
        }
    }
}