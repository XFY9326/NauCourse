package tool.xfy9326.naucourses.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.io.prefs.AppPref
import tool.xfy9326.naucourses.providers.info.methods.*

object GlobalCacheLoader {
    suspend fun loadInitCache() = withContext(Dispatchers.Default) {
        launch { TermDateInfo.getInfo() }
        launch { PersonalInfo.getInfo() }
        launch { CardBalanceInfo.getInfo() }
        launch { NewsInfo.getInfo(AppPref.readShowNewsType()) }
        launch { CourseInfo.getInfo(CourseInfo.OperationType.INIT_DATA) }
    }
}