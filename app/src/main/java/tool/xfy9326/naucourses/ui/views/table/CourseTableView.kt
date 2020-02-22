package tool.xfy9326.naucourses.ui.views.table

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.gridlayout.widget.GridLayout


class CourseTableView : GridLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun addViews(views: Array<View>) {
        for (view in views) {
            addViewInLayout(view, -1, view.layoutParams, true)
        }
        requestLayout()
        invalidate()
    }
}