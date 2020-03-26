package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import tool.xfy9326.naucourse.R

class AdvancedSwipeRefreshLayout : SwipeRefreshLayout {
    companion object {
        private const val ATTR_DEFAULT_RES_ID = 0
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context, attrs)
    }

    private fun setAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdvancedSwipeRefreshLayout)
            val colorSchemeResId = typedArray.getResourceId(
                R.styleable.AdvancedSwipeRefreshLayout_color_scheme,
                ATTR_DEFAULT_RES_ID
            )
            val triggerAsyncDistanceResId = typedArray.getResourceId(
                R.styleable.AdvancedSwipeRefreshLayout_trigger_async_distance,
                ATTR_DEFAULT_RES_ID
            )
            if (colorSchemeResId != ATTR_DEFAULT_RES_ID) {
                setColorSchemeColors(*context.resources.getIntArray(colorSchemeResId))
            }
            if (triggerAsyncDistanceResId != ATTR_DEFAULT_RES_ID) {
                setDistanceToTriggerSync(context.resources.getInteger(triggerAsyncDistanceResId))
            }
            typedArray.recycle()
        }
    }
}