@file:Suppress("unused")

package tool.xfy9326.naucourse.kt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


fun Context.showShortToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(this, getString(resId, *params), Toast.LENGTH_SHORT).show()

fun Context.showLongToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(this, getString(resId, *params), Toast.LENGTH_LONG).show()

fun Fragment.showShortToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(requireContext(), getString(resId, *params), Toast.LENGTH_SHORT).show()

fun Fragment.showLongToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(requireContext(), getString(resId, *params), Toast.LENGTH_LONG).show()

fun AppCompatActivity.enableHomeButton() {
    supportActionBar?.apply {
        setHomeButtonEnabled(true)
        setDisplayHomeAsUpEnabled(true)
    }
}

fun AppCompatActivity.disableActionBarTitle() {
    supportActionBar?.setDisplayShowTitleEnabled(false)
}

fun Context.tryStartActivity(intent: Intent, options: Bundle? = null): Boolean {
    if (intent.resolveActivity(packageManager) != null) {
        ContextCompat.startActivity(this, intent, options)
        return true
    }
    return false
}