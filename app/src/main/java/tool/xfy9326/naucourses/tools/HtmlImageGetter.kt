package tool.xfy9326.naucourses.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
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
    private val contextReference = WeakReference<Context>(context)
    private val drawableList = Vector<BitmapDrawable>()

    companion object {
        private const val BITMAP_MAX_PERCENT = 2 / 3f
        private const val BITMAP_MIN_PERCENT = 1 / 2f
        private const val BITMAP_MAX_ZOOM = 3f

        private const val DRAWABLE_LEVEL_LOADING = 1
        private const val DRAWABLE_LEVEL_BROKEN = 2
        private const val DRAWABLE_LEVEL_SUCCESS = 3
    }

    override fun getDrawable(source: String?): Drawable {
        val drawable = LevelListDrawable().apply {
            val defaultIcon = contextReference.get()?.getDrawable(R.drawable.ic_image)!!
            addLevel(
                DRAWABLE_LEVEL_LOADING,
                DRAWABLE_LEVEL_LOADING, defaultIcon
            )
            setBounds(0, 0, defaultIcon.intrinsicWidth, defaultIcon.intrinsicHeight)
            level = DRAWABLE_LEVEL_LOADING
        }
        if (source != null) {
            scope.launch(Dispatchers.Default) {
                refreshTextView()
                val bitmap = NewsInfo.getImageForNewsInfo(source, newsType)
                if (bitmap != null) {
                    drawable.apply {
                        val bitmapDrawable = BitmapDrawable(contextReference.get()?.resources, resizeBitmap(bitmap))
                        drawableList.add(bitmapDrawable)
                        addLevel(
                            DRAWABLE_LEVEL_SUCCESS,
                            DRAWABLE_LEVEL_SUCCESS, bitmapDrawable
                        )
                        setBounds(0, 0, bitmapDrawable.intrinsicWidth, bitmapDrawable.intrinsicHeight)
                        level = DRAWABLE_LEVEL_SUCCESS
                    }
                } else {
                    drawable.apply {
                        val icon = contextReference.get()?.getDrawable(R.drawable.ic_broken_image)!!
                        addLevel(
                            DRAWABLE_LEVEL_BROKEN,
                            DRAWABLE_LEVEL_BROKEN, icon
                        )
                        setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                        level = DRAWABLE_LEVEL_BROKEN
                    }
                }
                refreshTextView()
            }
        }
        return drawable
    }

    private suspend fun refreshTextView() = withContext(Dispatchers.Main) {
        synchronized(this) {
            textView.requestLayout()
            textView.text = textView.text
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val matrix = Matrix()
        val zoom = if (width > height) {
            val screenWidth = contextReference.get()!!.resources.displayMetrics.widthPixels
            (screenWidth * BITMAP_MAX_PERCENT) / width
        } else {
            val screenHeight = contextReference.get()!!.resources.displayMetrics.heightPixels
            (screenHeight * BITMAP_MIN_PERCENT) / height
        }
        if (zoom > BITMAP_MAX_ZOOM) {
            matrix.postScale(
                BITMAP_MAX_ZOOM,
                BITMAP_MAX_ZOOM
            )
        } else {
            matrix.postScale(zoom, zoom)
        }
        val result = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        bitmap.recycle()
        return result
    }

    fun recycleDrawable() {
        if (drawableList.isNotEmpty()) {
            for (drawable in drawableList) {
                try {
                    if (!drawable.bitmap.isRecycled) drawable.bitmap.recycle()
                } catch (e: Exception) {
                }
            }
        }
    }
}