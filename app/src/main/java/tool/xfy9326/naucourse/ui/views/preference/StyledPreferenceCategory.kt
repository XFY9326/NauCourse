package tool.xfy9326.naucourse.ui.views.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import tool.xfy9326.naucourse.R

@Suppress("unused")
class StyledPreferenceCategory : PreferenceCategory {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.itemView?.findViewById<TextView>(android.R.id.title)?.setTextColor(ContextCompat.getColor(context, R.color.colorPreferenceText))
    }
}