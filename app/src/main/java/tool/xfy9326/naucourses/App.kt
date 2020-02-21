package tool.xfy9326.naucourses

import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.io.dbHelpers.db.AppDB
import tool.xfy9326.naucourses.io.dbHelpers.db.CoursesDB
import tool.xfy9326.naucourses.io.dbHelpers.db.NetworkDB
import tool.xfy9326.naucourses.io.json.GsonStoreManager
import tool.xfy9326.naucourses.io.prefs.UserPref
import tool.xfy9326.naucourses.io.prefs.base.BasePref
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.providers.GlobalCacheLoader
import tool.xfy9326.naucourses.utils.secure.AccountUtils


@Suppress("UNUSED")
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            initConfigInstance()
            initDBInstance()
            initNetworkInstance()
            loadCache()
        }
    }

    private fun initConfigInstance() {
        BasePref.initContext(this)
        GsonStoreManager.initInstance(this)
    }

    private fun initDBInstance() {
        AppDB.initInstance(this)
        NetworkDB.initInstance(this)
        CoursesDB.initInstance(this)
    }

    private fun initNetworkInstance() {
        if (UserPref.HasLogin) {
            SSONetworkManager.initInstance(this, true, AccountUtils.readUserInfo().toLoginInfo())
        } else {
            SSONetworkManager.initInstance(this, false)
        }
    }

    private suspend fun loadCache() {
        if (UserPref.HasLogin) {
            GlobalCacheLoader.loadStoredGlobalCache()
        }
    }
}