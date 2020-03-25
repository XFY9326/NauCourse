package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet

class CourseCellLayout : AdvancedLinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    var rowNum: Int? = null
    var colNum: Int? = null
}