package tool.xfy9326.naucourse.ui.views.html

import android.graphics.Bitmap
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import org.xml.sax.XMLReader
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.BitmapUtils
import java.util.*

class AdvancedTagHandler : Html.TagHandler {
    private var longPressListener: OnImageLongPressListener? = null
    private var clickListener: OnImageClickListener? = null

    override fun handleTag(opening: Boolean, tag: String?, output: Editable?, xmlReader: XMLReader?) {
        if (Constants.HTML.ELEMENT_TAG_IMG == tag?.toLowerCase(Locale.getDefault()) && output != null) {
            val len = output.length
            output.getSpans(len - 1, len, ImageSpan::class.java).firstOrNull()?.let {
                output.setSpan(object : AdvancedClickableSpan() {
                    override fun onClick(widget: View, x: Int, y: Int) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val htmlDrawable = it.drawable as HtmlDrawable?
                            if (htmlDrawable?.downloadUrl != null && htmlDrawable.nowStatus == ImageStatus.SHOWING) {
                                clickListener?.onHtmlTextImageClick(htmlDrawable.downloadUrl!!)
                                super.onClick(widget, x, y)
                            }
                        } catch (e: Exception) {
                            ExceptionUtils.printStackTrace<AdvancedTagHandler>(e)
                        }
                    }

                    override fun onLongPress(widget: View, x: Int, y: Int) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val htmlDrawable = it.drawable as HtmlDrawable?
                            if (htmlDrawable?.downloadUrl != null && htmlDrawable.nowStatus == ImageStatus.SHOWING) {
                                longPressListener?.onHtmlTextImageLongPress(
                                    htmlDrawable.downloadUrl!!,
                                    BitmapUtils.getBitmapFromDrawable(htmlDrawable.drawable)!!
                                )
                                super.onLongPress(widget, x, y)
                            }
                        } catch (e: Exception) {
                            ExceptionUtils.printStackTrace<AdvancedTagHandler>(e)
                        }
                    }
                }, len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun setOnImageLongPressListener(listener: OnImageLongPressListener?) {
        this.longPressListener = listener
    }

    fun setOnImageClickListener(listener: OnImageClickListener?) {
        this.clickListener = listener
    }

    interface OnImageLongPressListener {
        fun onHtmlTextImageLongPress(source: String, bitmap: Bitmap)
    }

    interface OnImageClickListener {
        fun onHtmlTextImageClick(source: String)
    }
}