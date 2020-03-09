package tool.xfy9326.naucourses.ui.views.html

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.info.methods.NewsInfo
import java.lang.ref.WeakReference
import java.util.*

class HtmlImageGetter(
    private val scope: CoroutineScope, private val textView: TextView,
    context: Context, private val newsType: GeneralNews.PostSource
) : Html.ImageGetter {
    private val contextReference = WeakReference(context)
    private val drawableList = Vector<MutableDrawable<*>>()

    companion object {
        private const val BITMAP_MAX_PERCENT = 7 / 12f
        private const val BITMAP_MIN_PERCENT = 1 / 2f
        private const val BITMAP_MAX_ZOOM = 3f
    }

    enum class ImageStatus {
        LOADING,
        SHOWING,
        BROKEN
    }

    override fun getDrawable(source: String?): Drawable {
        val drawable = MutableDrawable<ImageStatus>().apply {
            val defaultIcon = contextReference.get()?.getDrawable(R.drawable.ic_image)!!
            setDrawable(defaultIcon)
            setBounds(0, 0, defaultIcon.intrinsicWidth, defaultIcon.intrinsicHeight)
            nowStatus = ImageStatus.LOADING
        }
        drawableList.add(drawable)

        if (source != null) {
            scope.launch(Dispatchers.Default) {
                refreshTextView()
                val bitmap = NewsInfo.getImageForNewsInfo(source, newsType)
                if (bitmap != null) {
                    drawable.apply {
                        val bitmapDrawable = BitmapDrawable(contextReference.get()?.resources, bitmap)

                        val screenWidth = contextReference.get()!!.resources.displayMetrics.widthPixels
                        val screenHeight = contextReference.get()!!.resources.displayMetrics.heightPixels

                        var zoom = if (screenWidth < screenHeight) {
                            (screenWidth * BITMAP_MAX_PERCENT) / bitmapDrawable.intrinsicWidth
                        } else {
                            (screenHeight * BITMAP_MIN_PERCENT) / bitmapDrawable.intrinsicHeight
                        }

                        if (zoom > BITMAP_MAX_ZOOM) {
                            zoom = BITMAP_MAX_ZOOM
                        }

                        setDrawable(bitmapDrawable)
                        setBounds(0, 0, (bitmapDrawable.intrinsicWidth * zoom).toInt(), (bitmapDrawable.intrinsicHeight * zoom).toInt())
                        nowStatus = ImageStatus.SHOWING
                    }
                } else {
                    drawable.apply {
                        val icon = contextReference.get()?.getDrawable(R.drawable.ic_broken_image)!!
                        setDrawable(icon)
                        setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                        nowStatus = ImageStatus.BROKEN
                    }
                }
                refreshTextView()
            }
        }
        return drawable
    }

    private suspend fun refreshTextView() = withContext(Dispatchers.Main) {
        synchronized(this@HtmlImageGetter) {
            textView.text = textView.text
            textView.requestLayout()
        }
    }

    fun recycleDrawable() {
        if (drawableList.isNotEmpty()) {
            for (drawable in drawableList) {
                if (drawable is BitmapDrawable) {
                    try {
                        if (!drawable.bitmap.isRecycled) drawable.bitmap.recycle()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}