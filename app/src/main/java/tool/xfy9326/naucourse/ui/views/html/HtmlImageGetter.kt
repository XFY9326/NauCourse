package tool.xfy9326.naucourse.ui.views.html

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.info.methods.NewsInfo
import tool.xfy9326.naucourse.tools.glide.ClientRequest
import kotlin.math.min

class HtmlImageGetter(private val context: Context, private val textView: TextView, private val newsType: PostSource) : Html.ImageGetter {
    companion object {
        private const val BITMAP_MAX_PERCENT = 7 / 12f
        private const val BITMAP_MIN_PERCENT = 1 / 2f
        private const val BITMAP_MAX_ZOOM = 3f
    }

    private val defaultIcon = context.getDrawable(R.drawable.ic_image)!!
    private val errorIcon = context.getDrawable(R.drawable.ic_broken_image)!!
    private val screenWidth = context.resources.displayMetrics.widthPixels
    private val screenHeight = context.resources.displayMetrics.heightPixels

    override fun getDrawable(source: String?): Drawable {
        val drawable = HtmlDrawable()
        val clientType = NewsInfo.getLoginClientTypeByPostSource(newsType)
        drawable.clientType = clientType

        if (source != null) {
            refreshTextView()
            val url = NewsInfo.getImageUrlForNewsInfo(source, newsType)
            val target = object : CustomTarget<Bitmap>() {
                override fun onLoadStarted(placeholder: Drawable?) {
                    drawable.updateDrawable(placeholder, ImageStatus.LOADING)
                    refreshTextView()
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    drawable.apply {
                        downloadUrl = url.toString()

                        val zoom = min(
                            if (screenWidth < screenHeight) {
                                (screenWidth * BITMAP_MAX_PERCENT) / resource.width
                            } else {
                                (screenHeight * BITMAP_MIN_PERCENT) / resource.height
                            }, BITMAP_MAX_ZOOM
                        )

                        val newWidth = (resource.width * zoom).toInt()
                        val newHeight = (resource.height * zoom).toInt()

                        updateDrawable(
                            BitmapDrawable(context.resources, Bitmap.createScaledBitmap(resource, newWidth, newHeight, true)),
                            ImageStatus.SHOWING,
                            newWidth,
                            newHeight
                        )
                    }
                    refreshTextView()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    drawable.updateDrawable(errorDrawable, ImageStatus.BROKEN)
                    refreshTextView()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    drawable.updateDrawable(placeholder, ImageStatus.LOADING)
                    refreshTextView()
                }
            }
            Glide.with(context).asBitmap().load(
                if (clientType == null) {
                    url.toString()
                } else {
                    ClientRequest(clientType, url.toString())
                }
            ).placeholder(defaultIcon).error(errorIcon).into(target)
        } else {
            drawable.updateDrawable(errorIcon, ImageStatus.BROKEN)
        }
        return drawable
    }

    private fun refreshTextView() =
        synchronized(this@HtmlImageGetter) {
            textView.text = textView.text
            textView.requestLayout()
        }
}