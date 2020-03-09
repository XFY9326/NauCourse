package tool.xfy9326.naucourses.ui.views.html

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import org.xml.sax.XMLReader
import tool.xfy9326.naucourses.Constants
import java.util.*

class AdvancedTagHandler : Html.TagHandler {
    private var listener: OnImageLongPressListener? = null

    override fun handleTag(opening: Boolean, tag: String?, output: Editable?, xmlReader: XMLReader?) {
        if (Constants.HTML.ELEMENT_TAG_IMG == tag?.toLowerCase(Locale.getDefault()) && output != null) {
            val len = output.length
            output.getSpans(len - 1, len, ImageSpan::class.java).firstOrNull()?.let {
                output.setSpan(object : AdvancedClickableSpan() {
                    override fun onLongPress(widget: View, x: Int, y: Int) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val mutableDrawable = it.drawable as MutableDrawable<HtmlImageGetter.ImageStatus>?
                            if (mutableDrawable?.nowStatus == HtmlImageGetter.ImageStatus.SHOWING) {
                                listener?.onHtmlTextImageLongPress(it.source!!, (mutableDrawable.drawable as BitmapDrawable?)?.bitmap!!)
                                super.onLongPress(widget, x, y)
                            }
                        } catch (e: Exception) {
                        }
                    }
                }, len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun setOnImageLongPressListener(listener: OnImageLongPressListener?) {
        this.listener = listener
    }

    interface OnImageLongPressListener {
        // Bitmap不需要回收，通过HtmlImageGetter统一回收
        fun onHtmlTextImageLongPress(source: String, bitmap: Bitmap)
    }
}