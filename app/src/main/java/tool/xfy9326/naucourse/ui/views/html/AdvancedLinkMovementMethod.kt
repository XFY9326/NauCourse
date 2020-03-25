package tool.xfy9326.naucourse.ui.views.html

import android.os.Handler
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.TextView
import androidx.core.text.getSpans


object AdvancedLinkMovementMethod : LinkMovementMethod() {
    private val longClickHandler = Handler()
    private var isLongPress = false

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        if (event != null && widget != null) {
            if (event.action == MotionEvent.ACTION_CANCEL) {
                clearHandler()
            } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x.toInt() - widget.totalPaddingLeft + widget.scrollX
                val y = event.y.toInt() - widget.totalPaddingTop + widget.scrollY
                val layout = widget.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())

                val link = buffer?.getSpans<AdvancedClickableSpan>(off, off)?.firstOrNull()
                val image = buffer?.getSpans<ImageSpan>(off, off)?.firstOrNull()
                if (link != null && image != null) {
                    // 修复ImageSpan点击判断以一整行来计算的问题（由于使用左对齐因此可以这样计算）
                    return if (image.drawable.bounds.right >= x && image.drawable.bounds.left <= x) {
                        if (event.action == MotionEvent.ACTION_UP) {
                            clearHandler()
                            if (!isLongPress) link.onClick(widget, x, y)
                            isLongPress = false
                        } else {
                            longClickHandler.postDelayed({
                                link.onLongPress(widget, x, y)
                                isLongPress = true
                            }, ViewConfiguration.getLongPressTimeout().toLong())
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

    fun clearHandler() = longClickHandler.removeCallbacksAndMessages(null)
}