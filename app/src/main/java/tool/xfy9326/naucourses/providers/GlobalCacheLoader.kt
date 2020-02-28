package tool.xfy9326.naucourses.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.providers.info.methods.CardBalanceInfo
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourses.providers.store.CourseTableStore

object GlobalCacheLoader {
    suspend fun loadInitCache() = withContext(Dispatchers.Default) {
        launch { TermDateInfo.getInfo() }
        launch { PersonalInfo.getInfo() }
        launch { CardBalanceInfo.getInfo() }
        launch { CourseInfo.getInfo(CourseInfo.OperationType.INIT_DATA) }
    }

    suspend fun loadStoredGlobalCache() = withContext(Dispatchers.Default) {
        launch { TermDateInfo.initCache() }
        launch { PersonalInfo.initCache() }
        launch { CardBalanceInfo.initCache() }
        launch { CourseInfo.initCache() }
        launch { CourseTableStore.initStore() }
    }
}