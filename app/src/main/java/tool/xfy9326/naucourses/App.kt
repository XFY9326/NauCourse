package tool.xfy9326.naucourses

import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.io.prefs.UserPref
import tool.xfy9326.naucourses.providers.GlobalCacheLoader

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        GlobalScope.launch {
            if (UserPref.HasLogin) {
                GlobalCacheLoader.loadStoredGlobalCache()
            }
        }
    }
}