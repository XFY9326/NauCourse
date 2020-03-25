package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

@Suppress("unused")
class AdvancedFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun addViewInLayout(view: View, requestLayout: Boolean = false) {
        addViewInLayout(view, -1, view.layoutParams, !requestLayout)
    }

    fun refreshLayout() {
        invalidate()
        requestLayout()
    }
}