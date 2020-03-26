package tool.xfy9326.naucourse

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.tools.NotifyLivaData
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils

class App : Application() {
    val nightModeChanged = NotifyLivaData()
    val courseStyleTermUpdate = NotifyLivaData()
    val requestRebuildCourseTable = NotifyLivaData()

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ExceptionUtils.initCrashHandler()
        setupNightMode()
    }

    private fun setupNightMode() = AppCompatDelegate.setDefaultNightMode(BaseUtils.getNightModeInt(SettingsPref.getNightMode()))
}