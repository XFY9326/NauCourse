package tool.xfy9326.naucourse.ui.views.html

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.info.methods.NewsInfo
import kotlin.math.min

class HtmlImageGetter(private val context: Context, private val textView: TextView, private val newsType: GeneralNews.PostSource) : Html.ImageGetter {
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

        if (source != null) {
            refreshTextView()
            val url = NewsInfo.getImageUrlForNewsInfo(source, newsType)
            val target = object : CustomTarget<Drawable>() {
                override fun onLoadStarted(placeholder: Drawable?) {
                    drawable.updateDrawable(placeholder, ImageStatus.LOADING)
                    refreshTextView()
                }

                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    drawable.apply {
                        downloadUrl = url.toString()

                        val zoom = min(
                            if (screenWidth < screenHeight) {
                                (screenWidth * BITMAP_MAX_PERCENT) / resource.intrinsicWidth
                            } else {
                                (screenHeight * BITMAP_MIN_PERCENT) / resource.intrinsicHeight
                            }, BITMAP_MAX_ZOOM
                        )

                        updateDrawable(resource, ImageStatus.SHOWING)
                        setBounds(0, 0, (resource.intrinsicWidth * zoom).toInt(), (resource.intrinsicHeight * zoom).toInt())
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
            Glide.with(context).load(url.toString()).placeholder(defaultIcon).error(errorIcon).into(target)
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