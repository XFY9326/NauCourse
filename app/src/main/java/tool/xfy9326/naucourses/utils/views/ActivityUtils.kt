package tool.xfy9326.naucourses.utils.views

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar


object ActivityUtils {
    fun Activity.showSnackBar(view: View, text: String) =
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()

    fun Activity.showSnackBar(view: View, @StringRes strId: Int) =
        Snackbar.make(view, strId, Snackbar.LENGTH_SHORT).show()

    fun Activity.showToast(text: String) =
        showToast(this, text)

    fun Activity.showToast(@StringRes resId: Int) =
        showToast(this, resId)

    fun showToast(context: Context, text: String) =
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    fun showToast(context: Context, @StringRes resId: Int) =
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()

    fun AppCompatActivity.enableHomeButton() {
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }
}