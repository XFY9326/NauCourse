package tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class ListRecyclerAdapter<VH : RecyclerView.ViewHolder, E>(context: Context) : RecyclerView.Adapter<VH>() {
    private val layoutInflater = LayoutInflater.from(context)
    private val dataLock = Any()

    @Volatile
    private var dataContainer = emptyList<E>()

    @CallSuper
    open fun updateData(data: List<E>) {
        synchronized(dataLock) {
            dataContainer = data
            notifyDataSetChanged()
        }
    }

    final override fun getItemCount(): Int = synchronized(dataLock) { dataContainer.size }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        onCreateViewHolder(layoutInflater.inflate(onBindLayout(), parent, false))

    @Synchronized
    final override fun onBindViewHolder(holder: VH, position: Int) {
        synchronized(dataLock) {
            onBindViewHolder(holder, position, dataContainer[holder.adapterPosition])
        }
    }

    @LayoutRes
    protected abstract fun onBindLayout(): Int

    protected abstract fun onCreateViewHolder(view: View): VH

    protected abstract fun onBindViewHolder(holder: VH, position: Int, element: E)
}