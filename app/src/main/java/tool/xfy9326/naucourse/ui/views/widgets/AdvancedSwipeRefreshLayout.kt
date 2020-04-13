package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import tool.xfy9326.naucourse.R
import kotlin.math.abs


class AdvancedSwipeRefreshLayout : SwipeRefreshLayout {
    private var mTouchSlop = 0
    private var startY = 0f
    private var startX = 0f
    private var mIsVpDrag = false

    companion object {
        private const val ATTR_DEFAULT_RES_ID = 0
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context, attrs)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.y
                startX = ev.x
                mIsVpDrag = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsVpDrag) {
                    return false
                }
                val endY = ev.y
                val endX = ev.x
                val distanceX = abs(endX - startX)
                val distanceY = abs(endY - startY)
                if (distanceX > mTouchSlop && distanceX > distanceY) {
                    mIsVpDrag = true
                    return false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mIsVpDrag = false
        }
        return super.onInterceptTouchEvent(ev)
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