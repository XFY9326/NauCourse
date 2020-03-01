package tool.xfy9326.naucourses.utils.views

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.view_toast.view.*
import tool.xfy9326.naucourses.R


object ActivityUtils {

    fun Activity.showSnackBar(view: CoordinatorLayout, @StringRes strId: Int) =
        Snackbar.make(view, strId, Snackbar.LENGTH_SHORT).show()

    fun Fragment.showSnackBar(view: CoordinatorLayout, @StringRes strId: Int) =
        Snackbar.make(view, strId, Snackbar.LENGTH_SHORT).show()

    fun Activity.showToast(@StringRes resId: Int) =
        showToast(this, resId)

    fun showToast(context: Context, @StringRes resId: Int) = Toast(context.applicationContext).apply {
        LayoutInflater.from(context).apply {
            view = inflate(R.layout.view_toast, null).apply {
                tv_toastText.text = context.getString(resId)
            }
        }
        duration = Toast.LENGTH_SHORT
        show()
    }

    fun AppCompatActivity.enableHomeButton() {
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    fun AppCompatActivity.disableActionBarTitle() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}