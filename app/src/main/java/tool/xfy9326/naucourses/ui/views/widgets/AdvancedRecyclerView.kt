package tool.xfy9326.naucourses.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourses.R
import kotlin.properties.Delegates

class AdvancedRecyclerView : RecyclerView {
    companion object {
        private const val ATTR_DEFAULT_RES_ID = 0
    }

    private var emptyViewResId by Delegates.notNull<Int>()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setAttrs(context, attrs)
    }

    private fun setAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdvancedRecyclerView)
            emptyViewResId = typedArray.getResourceId(
                R.styleable.AdvancedRecyclerView_empty_view,
                ATTR_DEFAULT_RES_ID
            )
            typedArray.recycle()
        }
    }

    private val emptyViewAdapterObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            modifyEmptyView()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (this.adapter?.hasObservers() == true) {
            this.adapter?.unregisterAdapterDataObserver(emptyViewAdapterObserver)
        }
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyViewAdapterObserver)
    }

    @Synchronized
    private fun modifyEmptyView() {
        if (parent != null) {
            val emptyView = (parent as View).findViewById<View>(emptyViewResId)
            if (emptyView != null) {
                if (adapter == null || adapter!!.itemCount == 0) {
                    visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            }
        }
    }
}