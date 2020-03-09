package tool.xfy9326.naucourses.ui.views.html

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.accessibility.AccessibilityEvent

abstract class AdvancedClickableSpan : CharacterStyle() {
    open fun onClick(widget: View, x: Int, y: Int) {
        widget.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
    }

    open fun onLongPress(widget: View, x: Int, y: Int) {
        widget.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)
        widget.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    override fun updateDrawState(tp: TextPaint?) {}
}