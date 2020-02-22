package tool.xfy9326.naucourses.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.providers.info.methods.CardBalanceInfo
import tool.xfy9326.naucourses.providers.info.methods.CourseInfo
import tool.xfy9326.naucourses.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourses.providers.info.methods.TermDateInfo
import tool.xfy9326.naucourses.providers.store.CourseTableGsonStore

object GlobalCacheLoader {
    suspend fun loadInitCache() = withContext(Dispatchers.Default) {
        TermDateInfo.getInfo()
        PersonalInfo.getInfo()
        CardBalanceInfo.getInfo()
        CourseInfo.getInfo(CourseInfo.OperationType.INIT_DATA)
    }

    suspend fun loadStoredGlobalCache() = withContext(Dispatchers.Default) {
        TermDateInfo.initCache()
        PersonalInfo.initCache()
        CardBalanceInfo.initCache()
        CourseInfo.initCache()
        CourseTableGsonStore.initStore()
    }
}