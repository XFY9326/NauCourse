package tool.xfy9326.naucourse.utils.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.*
import android.os.Build
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.R

object ViewUtils {
    const val COURSE_DATA_JOIN_SYMBOL = "·"

    fun tryStartAnimateDrawable(drawable: Drawable) {
        if (drawable is AnimatedVectorDrawable) {
            drawable.start()
        } else if (drawable is AnimatedVectorDrawableCompat) {
            drawable.start()
        } else if (drawable is Animatable) {
            drawable.start()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && drawable is Animatable2) {
            drawable.start()
        } else if (drawable is Animatable2Compat) {
            drawable.start()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
            drawable.start()
        }
    }

    fun tryStopAnimateDrawable(drawable: Drawable) {
        if (drawable is AnimatedVectorDrawable) {
            drawable.stop()
        } else if (drawable is AnimatedVectorDrawableCompat) {
            drawable.stop()
        } else if (drawable is Animatable) {
            drawable.stop()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && drawable is Animatable2) {
            drawable.stop()
        } else if (drawable is Animatable2Compat) {
            drawable.stop()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
            drawable.stop()
        }
    }

    fun getActionBarSize(context: Context): Int {
        val styledAttributes: TypedArray = context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val mActionBarSize = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        return mActionBarSize
    }

    fun getCourseDataShowText(text: String) =
        if (text.isEmpty() || text.isBlank()) {
            App.instance.getString(R.string.no_data)
        } else {
            if (COURSE_DATA_JOIN_SYMBOL in text) {
                val trimText = text.trim()
                val newText = when {
                    trimText.startsWith(COURSE_DATA_JOIN_SYMBOL) ->
                        text.substring(text.indexOf(COURSE_DATA_JOIN_SYMBOL) + 1)
                    trimText.endsWith(COURSE_DATA_JOIN_SYMBOL) ->
                        text.substring(0, text.lastIndexOf(COURSE_DATA_JOIN_SYMBOL))
                    else -> text
                }
                if (newText.isEmpty() || newText.isBlank()) {
                    App.instance.getString(R.string.no_data)
                } else {
                    newText
                }
            } else {
                text
            }
        }
}