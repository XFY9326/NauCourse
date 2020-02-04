package tool.xfy9326.naucourses

import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.io.configs.PreferenceConfig
import tool.xfy9326.naucourses.io.dbHelpers.db.AppDB
import tool.xfy9326.naucourses.io.dbHelpers.db.CoursesDB
import tool.xfy9326.naucourses.io.dbHelpers.db.NetworkDB
import tool.xfy9326.naucourses.network.NauNetworkManager
import tool.xfy9326.naucourses.network.clients.base.LoginInfo

@Suppress("UNUSED")
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initInstance()
        setLoginInfo()
    }

    private fun initInstance() {
        AppDB.initInstance(this)
        NetworkDB.initInstance(this)
        CoursesDB.initInstance(this)
        PreferenceConfig.initInstance(this)
        NauNetworkManager.initInstance(this)
    }

    private fun setLoginInfo() {
        GlobalScope.launch {
            NauNetworkManager.getInstance().ssoLogin(LoginInfo("17013209", "262010"))
        }
    }
}