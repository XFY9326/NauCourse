package tool.xfy9326.naucourse.ui.views.widgets

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

// 参考DrawableWrapper实现，用于可替换的Drawable显示
open class MutableDrawable<T : Enum<*>>(drawable: Drawable? = null, nowStatus: T? = null) : Drawable(), Drawable.Callback {
    @Volatile
    var drawable: Drawable? = drawable
        private set

    @Volatile
    var nowStatus: T? = nowStatus
        private set

    @Synchronized
    open fun setDrawable(drawable: Drawable?, nowStatus: T?, recycleOld: Boolean = true) {
        if (drawable != this.drawable && nowStatus != this.nowStatus) {
            this.drawable?.let {
                it.callback = null
                if (recycleOld && it is BitmapDrawable) {
                    try {
                        if (!it.bitmap.isRecycled) it.bitmap.recycle()
                    } catch (e: Exception) {
                    }
                }
            }
            this.drawable = drawable
            this.nowStatus = nowStatus
            this.drawable?.callback = this
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) = drawable!!.draw(canvas)

    override fun onBoundsChange(bounds: Rect?) {
        drawable!!.bounds = bounds!!
    }

    override fun setChangingConfigurations(configs: Int) {
        drawable!!.changingConfigurations = configs
    }

    override fun getChangingConfigurations(): Int = drawable!!.changingConfigurations

    override fun setFilterBitmap(filter: Boolean) {
        drawable!!.isFilterBitmap = filter
    }

    override fun setAlpha(alpha: Int) {
        drawable!!.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        drawable!!.colorFilter = cf
    }

    override fun isStateful(): Boolean = drawable!!.isStateful

    override fun setState(stateSet: IntArray): Boolean = drawable!!.setState(stateSet)

    override fun getState(): IntArray = drawable!!.state

    override fun getCurrent(): Drawable = drawable!!.current

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        super.setVisible(visible, restart) || drawable!!.setVisible(visible, restart)

    override fun getTransparentRegion(): Region? = drawable!!.transparentRegion

    override fun getIntrinsicWidth(): Int = drawable!!.intrinsicWidth

    override fun getIntrinsicHeight(): Int = drawable!!.intrinsicHeight

    override fun getMinimumWidth(): Int = drawable!!.minimumWidth

    override fun getMinimumHeight(): Int = drawable!!.minimumHeight

    override fun getPadding(padding: Rect): Boolean = drawable!!.getPadding(padding)

    override fun onLevelChange(level: Int): Boolean = drawable!!.setLevel(level)

    override fun setAutoMirrored(mirrored: Boolean) = DrawableCompat.setAutoMirrored(drawable!!, mirrored)

    override fun isAutoMirrored(): Boolean = DrawableCompat.isAutoMirrored(drawable!!)

    override fun setTint(tint: Int) = DrawableCompat.setTint(drawable!!, tint)

    override fun setTintList(tint: ColorStateList?) = DrawableCompat.setTintList(drawable!!, tint)

    override fun setTintMode(tintMode: PorterDuff.Mode?) = DrawableCompat.setTintMode(drawable!!, tintMode!!)

    override fun setHotspot(x: Float, y: Float) = DrawableCompat.setHotspot(drawable!!, x, y)

    override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) =
        DrawableCompat.setHotspotBounds(drawable!!, left, top, right, bottom)

    @Suppress("DEPRECATION")
    override fun getOpacity(): Int = drawable!!.opacity

    override fun unscheduleDrawable(who: Drawable, what: Runnable) = unscheduleSelf(what)

    override fun invalidateDrawable(who: Drawable) = invalidateSelf()

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = scheduleSelf(what, `when`)
}