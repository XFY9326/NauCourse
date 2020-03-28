package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.gridlayout.widget.GridLayout


@Suppress("MemberVisibilityCanBePrivate")
class AdvancedGridLayout : GridLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun replaceAllViews(views: Array<out View>, refreshLayout: Boolean = true) {
        if (childCount != 0) {
            removeAllViewsInLayout()
        }
        for (view in views) {
            addViewInLayout(view, -1, view.layoutParams, true)
        }
        if (refreshLayout) refreshLayout()
    }

    fun refreshLayout() {
        requestLayout()
        invalidate()
    }
}