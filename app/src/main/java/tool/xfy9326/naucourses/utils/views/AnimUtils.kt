package tool.xfy9326.naucourses.utils.views

import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.view.animation.AnimationUtils

object AnimUtils {
    fun getAnimationFadeVisible(context: Context) = AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
        duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }

    fun getAnimationFadeGone(context: Context) = AnimationUtils.loadAnimation(context, android.R.anim.fade_out).apply {
        duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }

    fun getAnimationLoopCallback() = object : Animatable2.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            (drawable as AnimatedVectorDrawable).start()
        }
    }
}