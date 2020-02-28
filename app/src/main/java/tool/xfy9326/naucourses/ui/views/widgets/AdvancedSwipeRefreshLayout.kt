package tool.xfy9326.naucourses.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import tool.xfy9326.naucourses.R
import kotlin.properties.Delegates

class AdvancedSwipeRefreshLayout : SwipeRefreshLayout {
    companion object {
        private const val ATTR_DEFAULT_RES_ID = 0
    }

    private var colorSchemeResId by Delegates.notNull<Int>()
    private var triggerAsyncDistanceResId by Delegates.notNull<Int>()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context, attrs)
        applyAttrs()
    }

    private fun setAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdvancedSwipeRefreshLayout)
            colorSchemeResId = typedArray.getResourceId(
                R.styleable.AdvancedSwipeRefreshLayout_color_scheme,
                ATTR_DEFAULT_RES_ID
            )
            triggerAsyncDistanceResId = typedArray.getResourceId(
                R.styleable.AdvancedSwipeRefreshLayout_trigger_async_distance,
                ATTR_DEFAULT_RES_ID
            )
            typedArray.recycle()
        }
    }

    private fun applyAttrs() {
        if (colorSchemeResId != ATTR_DEFAULT_RES_ID) {
            setColorSchemeColors(*context.resources.getIntArray(colorSchemeResId))
        }
        if (triggerAsyncDistanceResId != ATTR_DEFAULT_RES_ID) {
            setDistanceToTriggerSync(context.resources.getInteger(triggerAsyncDistanceResId))
        }
    }

}