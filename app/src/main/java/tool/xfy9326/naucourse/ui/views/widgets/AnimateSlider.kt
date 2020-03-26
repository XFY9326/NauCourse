package tool.xfy9326.naucourse.ui.views.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.utils.views.AnimUtils

class AnimateSlider : Slider, Slider.OnSliderTouchListener {
    private var startValue = -1f
    private var listener: OnSlideFinishListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setAttrs(context)
    }

    private fun setAttrs(context: Context) {
        tickColor = ColorStateList.valueOf(Color.TRANSPARENT)
        haloColor = ColorStateList.valueOf(Color.TRANSPARENT)
        haloRadius = 0
        thumbRadius = context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_not_touched)
        trackColorActive = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorSliderTrackerActive))
        trackColorInactive = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorSliderTrackerInActive))
        trackHeight = context.resources.getDimensionPixelSize(R.dimen.slider_tracker_height)
        addOnSliderTouchListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> AnimUtils.animateSlideThumb(context, this, true)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> AnimUtils.animateSlideThumb(context, this, false)
        }
        return super.onTouchEvent(event)
    }

    override fun onStartTrackingTouch(slider: Slider) {
        startValue = slider.value
    }

    override fun onStopTrackingTouch(slider: Slider) {
        if (startValue != slider.value) {
            startValue = slider.value
            listener?.onValueChanged(this, slider.value)
        }
    }

    fun setOnSlideFinishListener(listener: OnSlideFinishListener) {
        this.listener = listener
    }

    interface OnSlideFinishListener {
        fun onValueChanged(slider: Slider, value: Float)
    }
}