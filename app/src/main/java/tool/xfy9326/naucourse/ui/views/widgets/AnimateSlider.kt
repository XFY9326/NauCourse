package tool.xfy9326.naucourse.ui.views.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.slider.Slider
import tool.xfy9326.naucourse.utils.views.AnimUtils

class AnimateSlider : Slider, Slider.OnSliderTouchListener {
    private var startValue = -1f
    private var listener: OnSlideFinishListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
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