package tool.xfy9326.naucourses.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.IBinder
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import tool.xfy9326.naucourses.io.prefs.SettingsPref
import kotlin.system.exitProcess


object BaseUtils {
    const val CRASH_RESTART_FLAG = "CRASH_RESTART"

    fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

    fun Int.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

    fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

    fun Int.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

    fun getNightModeInt(type: SettingsPref.NightModeType) =
        when (type) {
            SettingsPref.NightModeType.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            SettingsPref.NightModeType.ENABLED -> AppCompatDelegate.MODE_NIGHT_YES
            SettingsPref.NightModeType.DISABLED -> AppCompatDelegate.MODE_NIGHT_NO
        }

    fun restartApplication(context: Context, crashRestart: Boolean = false) {
        val packageManager: PackageManager = context.applicationContext.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (crashRestart) intent.putExtra(CRASH_RESTART_FLAG, true)
        context.applicationContext.startActivity(intent)
        exitProcess(0)
    }

    fun hideKeyboard(context: Context, windowToken: IBinder) =
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            if (isActive) hideSoftInputFromWindow(windowToken, 0)
        }
}