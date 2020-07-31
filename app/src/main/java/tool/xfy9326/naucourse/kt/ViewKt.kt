@file:Suppress("unused")

package tool.xfy9326.naucourse.kt

import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.constants.BaseConst


fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

fun Int.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

fun Int.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun EditText.clear() = this.setText(BaseConst.EMPTY)

fun TextView.clear() {
    this.text = BaseConst.EMPTY
}

suspend fun <T : View> T.runInMain(block: suspend (T) -> Unit) {
    withContext(Dispatchers.Main) {
        block.invoke(this@runInMain)
    }
}

fun CoordinatorLayout.showSnackBar(@StringRes strId: Int, vararg params: Any) =
    Snackbar.make(this, context.getString(strId, *params), Snackbar.LENGTH_SHORT).show()

fun CoordinatorLayout.showSnackBarWithCallback(@StringRes strId: Int, @StringRes actionStrId: Int, callback: (View) -> Unit) =
    Snackbar.make(this, strId, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(actionStrId, callback).show()