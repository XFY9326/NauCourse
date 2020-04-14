package tool.xfy9326.naucourse

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.providers.GlobalCacheManager
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.ImageUriUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils

class App : Application(), LifecycleObserver {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        ExceptionUtils.initCrashHandler()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        setupNightMode()
        GlobalScope.launch(Dispatchers.Default) {
            clearOldCache()
            IntentUtils.startNextCourseAlarm(this@App)
        }
    }

    private fun setupNightMode() = AppCompatDelegate.setDefaultNightMode(BaseUtils.getNightModeInt(SettingsPref.getNightMode()))

    private fun clearOldCache() = ImageUriUtils.clearLocalImageBySubDir(Constants.Image.DIR_SHARE_TEMP_IMAGE)

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForeground() {
        GlobalCacheManager.tryCancelCacheCleaner()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackground() {
        GlobalCacheManager.startCacheCleanerTimer()
    }

    override fun onTerminate() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onTerminate()
    }
}