package tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class ListRecyclerAdapter<VH : RecyclerView.ViewHolder, E>(context: Context, callBack: DiffUtil.ItemCallback<E>? = null) :
    ListAdapter<E, VH>(callBack ?: SimpleDifferItemCallBack<E>()) {
    protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    final override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, position, getItem(position))
    }

    protected abstract fun onBindViewHolder(holder: VH, position: Int, element: E)

    @SuppressLint("DiffUtilEquals")
    open class SimpleDifferItemCallBack<E> : DiffUtil.ItemCallback<E>() {
        override fun areContentsTheSame(oldItem: E, newItem: E): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: E, newItem: E): Boolean {
            return oldItem == newItem
        }
    }
}