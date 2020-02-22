package tool.xfy9326.naucourses.ui.views.table

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

class CourseTableCellLayout : LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun addViewWithoutRequestLayout(view: View) {
        addViewInLayout(view, -1, view.layoutParams, true)
    }
}