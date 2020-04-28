package tool.xfy9326.naucourse.ui.views.html

import android.graphics.drawable.Drawable
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.ui.views.widgets.MutableDrawable

class HtmlDrawable(drawable: Drawable? = null, nowStatus: ImageStatus? = null) : MutableDrawable<ImageStatus>(drawable, nowStatus) {
    var downloadUrl: String? = null
    var clientType: LoginNetworkManager.ClientType? = null

    fun updateDrawable(drawable: Drawable?, nowStatus: ImageStatus?) {
        super.setDrawable(drawable, nowStatus, false)
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    }

    fun updateDrawable(drawable: Drawable?, nowStatus: ImageStatus?, width: Int, height: Int) {
        drawable?.setBounds(0, 0, width, height)
        super.setDrawable(drawable, nowStatus, false)
        setBounds(0, 0, width, height)
    }
}