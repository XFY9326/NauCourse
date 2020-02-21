package tool.xfy9326.naucourses.utils.views

import android.widget.EditText
import android.widget.TextView
import tool.xfy9326.naucourses.Constants

object ViewUtils {
    fun EditText.clear() = this.setText(Constants.EMPTY)

    fun TextView.clear() {
        this.text = Constants.EMPTY
    }
}