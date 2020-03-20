package tool.xfy9326.naucourses.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import androidx.gridlayout.widget.GridLayout


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
            view.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
                duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            })
        }
        if (refreshLayout) refreshLayout()
    }

    fun refreshLayout() {
        requestLayout()
        invalidate()
    }
}