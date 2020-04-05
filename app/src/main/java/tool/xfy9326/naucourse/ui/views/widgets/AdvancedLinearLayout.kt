package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat

open class AdvancedLinearLayout : LinearLayoutCompat {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun addViewInLayout(view: View, requestLayout: Boolean = false) {
        addViewInLayout(view, -1, view.layoutParams, !requestLayout)
    }

    fun addViewsInLayout(views: Array<View>, requestLayout: Boolean = true) {
        for (view in views) {
            addViewInLayout(view, -1, view.layoutParams, true)
        }
        if (requestLayout) {
            refreshLayout()
        }
    }

    fun refreshLayout() {
        invalidate()
        requestLayout()
    }
}