package tool.xfy9326.naucourse

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.providers.GlobalCacheManager
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils

class App : Application(), LifecycleObserver {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initCrashReport()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        setupNightMode()

        runBlocking {
            LoginNetworkManager.initLoginInfo()
        }

        GlobalScope.launch(Dispatchers.Default) {
            clearOldCache()
            IntentUtils.startNextCourseAlarm(this@App)
        }
    }

    private fun initCrashReport() {
        ExceptionUtils.initCrashHandler()
        @Suppress("ConstantConditionIf")
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(this)
            CrashReport.setIsDevelopmentDevice(this, BuildConfig.DEBUG)
        }
    }

    private fun setupNightMode() = AppCompatDelegate.setDefaultNightMode(BaseUtils.getNightModeInt(SettingsPref.getNightMode()))

    private fun clearOldCache() = ImageUtils.clearLocalImageBySubDir(Constants.Image.DIR_SHARE_TEMP_IMAGE)

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