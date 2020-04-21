package tool.xfy9326.naucourse.ui.views.html

import android.graphics.drawable.Drawable
import tool.xfy9326.naucourse.ui.views.widgets.MutableDrawable

class HtmlDrawable(drawable: Drawable? = null, nowStatus: ImageStatus? = null) : MutableDrawable<ImageStatus>(drawable, nowStatus) {
    var downloadUrl: String? = null

    fun updateDrawable(drawable: Drawable?, nowStatus: ImageStatus?) {
        super.setDrawable(drawable, nowStatus, false)
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    }
}