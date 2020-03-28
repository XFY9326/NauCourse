package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import kotlin.properties.Delegates

class CourseCellLayout : AdvancedFrameLayout {
    var rowNum by Delegates.notNull<Int>()
        private set

    var colNum by Delegates.notNull<Int>()
        private set

    constructor(context: Context) : super(context)

    constructor(context: Context, rowNum: Int, colNum: Int) : super(context) {
        this.rowNum = rowNum
        this.colNum = colNum
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
}