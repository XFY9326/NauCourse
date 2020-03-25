package tool.xfy9326.naucourse.utils.views

import android.widget.EditText
import android.widget.TextView
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R

object ViewUtils {
    const val COURSE_DATA_JOIN_SYMBOL = "·"

    fun EditText.clear() = this.setText(Constants.EMPTY)

    fun TextView.clear() {
        this.text = Constants.EMPTY
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