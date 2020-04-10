package tool.xfy9326.naucourse

import android.app.Application
import android.app.UiModeManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        setupNightMode()
        ExceptionUtils.initCrashHandler()
        IntentUtils.startNextCourseAlarm(this)
    }

    private fun setupNightMode() {
        BaseUtils.getNightModeInt(SettingsPref.getNightMode()).let {
            getSystemService<UiModeManager>()?.nightMode = it
            AppCompatDelegate.setDefaultNightMode(it)
        }
    }
}