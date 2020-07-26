package tool.xfy9326.naucourse.utils.views

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object ActivityUtils {

    fun showSnackBar(context: Context, view: CoordinatorLayout, @StringRes strId: Int, vararg params: Any) =
        Snackbar.make(view, context.getString(strId, *params), Snackbar.LENGTH_SHORT).show()

    fun showSnackBar(view: CoordinatorLayout, @StringRes strId: Int, vararg params: Any) =
        Snackbar.make(view, view.context.getString(strId, *params), Snackbar.LENGTH_SHORT).show()

    fun showSnackBarWithCallback(view: CoordinatorLayout, @StringRes strId: Int, @StringRes actionStrId: Int, callback: View.OnClickListener) =
        Snackbar.make(view, strId, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(actionStrId, callback).show()

    fun Activity.showToast(@StringRes resId: Int, vararg params: Any) =
        showToast(this, resId, *params)

    fun Fragment.showToast(@StringRes resId: Int, vararg params: Any) =
        showToast(requireContext(), resId, *params)

    fun showToast(context: Context, @StringRes resId: Int, vararg params: Any) =
        Toast.makeText(context, context.getString(resId, *params), Toast.LENGTH_SHORT).show()

    fun AppCompatActivity.enableHomeButton() {
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    @Suppress("unused")
    fun AppCompatActivity.disableActionBarTitle() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}