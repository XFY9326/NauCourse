package tool.xfy9326.naucourses.ui.views.recyclerview

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourses.R

class SwipeDeleteCallback<T : RecyclerView.ViewHolder>(context: Context, private val listener: OnItemDeleteListener<T>) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {

    private val backgroundDrawable = context.getDrawable(R.color.colorPrimary)
    private val deleteIconDrawable = context.getDrawable(R.drawable.ic_delete)
    private val deleteText = context.getString(R.string.delete)

    override fun isLongPressDragEnabled(): Boolean = false
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
    }

    interface OnItemDeleteListener<T : RecyclerView.ViewHolder> {
        fun onDeleteItem(viewHolder: T, position: Int)
    }
}