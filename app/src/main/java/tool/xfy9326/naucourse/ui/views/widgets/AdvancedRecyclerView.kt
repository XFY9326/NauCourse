package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourse.R
import kotlin.math.abs
import kotlin.properties.Delegates

class AdvancedRecyclerView : RecyclerView {
    companion object {
        private const val ATTR_DEFAULT_RES_ID = 0
    }

    private var startX = 0f
    private var startY = 0f
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

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            modifyEmptyView()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            modifyEmptyView()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val firstAdapter = this.adapter == null
        if (this.adapter?.hasObservers() == true) {
            this.adapter?.unregisterAdapterDataObserver(emptyViewAdapterObserver)
        }
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyViewAdapterObserver)

        if (firstAdapter) {
            postDelayed({
                modifyEmptyView()
            }, 250)
        } else {
            modifyEmptyView()
        }
    }

    @Synchronized
    private fun modifyEmptyView() {
        if (parent != null) {
            val emptyView = (parent as View).findViewById<View>(emptyViewResId)
            if (emptyView != null) {
                isVisible = adapter != null && adapter!!.itemCount != 0
                emptyView.isVisible = !isVisible
            }
        }
    }

    // 修复ViewPager2滑动冲突
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x
                val endY = ev.y
                val disX = abs(endX - startX)
                val disY = abs(endY - startY)
                if (disX > disY) {
                    parent?.requestDisallowInterceptTouchEvent(canScrollHorizontally((startX - endX).toInt()))
                } else {
                    parent?.requestDisallowInterceptTouchEvent(canScrollVertically((startY - endY).toInt()))
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
        }
        return super.dispatchTouchEvent(ev)
    }
}