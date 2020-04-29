package tool.xfy9326.naucourse.ui.views.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.content.ContextCompat
import tool.xfy9326.naucourse.R
import kotlin.properties.Delegates

class CourseTimeEditCell : AppCompatCheckedTextView, View.OnClickListener {
    var showNum by Delegates.notNull<Int>()
        private set

    private var checkedListener: OnCheckedChangeListener? = null

    constructor(context: Context, showNum: Int, checked: Boolean) : super(context) {
        this.showNum = showNum
        text = showNum.toString()
        isChecked = checked
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        elevation = resources.getDimensionPixelSize(R.dimen.course_time_button_elevation).toFloat()
        gravity = Gravity.CENTER
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setBackgroundResource(R.drawable.bg_course_time)
        setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryTextWhenDark))
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        toggle()
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        checkedListener?.onCheckedChanged(this, checked)
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.checkedListener = listener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(cellView: CourseTimeEditCell, isChecked: Boolean)
    }
}