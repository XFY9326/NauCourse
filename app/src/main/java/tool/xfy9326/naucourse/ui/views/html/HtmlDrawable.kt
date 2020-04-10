package tool.xfy9326.naucourse.ui.views.html

import android.graphics.drawable.Drawable
import tool.xfy9326.naucourse.ui.views.widgets.MutableDrawable

class HtmlDrawable(drawable: Drawable? = null, nowStatus: ImageStatus? = null) : MutableDrawable<ImageStatus>(drawable, nowStatus) {
    var downloadUrl: String? = null
}