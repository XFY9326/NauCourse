package tool.xfy9326.naucourses

import android.app.Application
import tool.xfy9326.naucourses.tools.EventLiveData
import tool.xfy9326.naucourses.utils.debug.ExceptionUtils

class App : Application() {
    val courseTermUpdate = EventLiveData(false)

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ExceptionUtils.initCrashHandler()
    }
}