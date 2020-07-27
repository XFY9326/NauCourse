package tool.xfy9326.naucourse.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import kotlin.system.exitProcess

@Suppress("unused")
object BaseUtils {
    const val CRASH_RESTART_FLAG = "CRASH_RESTART"
    const val SHOW_ERROR_ACTIVITY_FLAG = "SHOW_ERROR_ACTIVITY_FLAG"

    fun setupNightMode() = AppCompatDelegate.setDefaultNightMode(getNightModeInt(SettingsPref.getNightMode()))

    fun clearCache(context: Context) = BaseIOUtils.deleteFile(context.cacheDir)

    fun isNightModeUsing(context: Context) =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    fun getNightModeInt(type: SettingsPref.NightModeType) =
        when (type) {
            SettingsPref.NightModeType.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            SettingsPref.NightModeType.ENABLED -> AppCompatDelegate.MODE_NIGHT_YES
            SettingsPref.NightModeType.DISABLED -> AppCompatDelegate.MODE_NIGHT_NO
        }

    fun restartApplication(crashRestart: Boolean = false, showErrorActivity: Boolean = false) {
        val intent = App.instance.packageManager.getLaunchIntentForPackage(App.instance.packageName)!!.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (crashRestart) putExtra(CRASH_RESTART_FLAG, true)
            if (showErrorActivity) putExtra(SHOW_ERROR_ACTIVITY_FLAG, true)
        }
        App.instance.startActivity(intent)
        if (crashRestart) exitProcess(0)
    }

    fun hideKeyboard(context: Context, windowToken: IBinder) =
        context.getSystemService<InputMethodManager>()?.apply {
            if (isActive) hideSoftInputFromWindow(windowToken, 0)
        }

    fun isBeta() = BuildConfig.FLAVOR == OthersConst.FLAVOR_BETA
}