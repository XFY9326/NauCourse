package tool.xfy9326.naucourse.utils.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.google.android.material.slider.Slider
import tool.xfy9326.naucourse.R

object AnimUtils {
    fun getAnimationFadeVisible(context: Context) = AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
        duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }!!

    fun getAnimationFadeGone(context: Context) = AnimationUtils.loadAnimation(context, android.R.anim.fade_out).apply {
        duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }!!

    fun getAnimationLoopCallback() = object : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            (drawable as AnimatedVectorDrawable).start()
        }
    }

    fun animateSlideThumb(context: Context, slider: Slider, isTouched: Boolean) {
        val radius = if (isTouched) {
            Pair(
                context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_not_touched),
                context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_touched)
            )
        } else {
            Pair(
                context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_touched),
                context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_not_touched)
            )
        }
        slider.clearAnimation()
        ValueAnimator.ofInt(radius.first, radius.second).apply {
            duration = context.resources.getInteger(R.integer.very_short_anim_time).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { slider.thumbRadius = it.animatedValue as Int }
            start()
        }
    }
}