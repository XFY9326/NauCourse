package tool.xfy9326.naucourse.providers

import android.os.Handler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.providers.info.methods.*

object GlobalCacheManager {
    private const val CACHE_CLEAN_TIMEOUT = 5000L
    private val cacheHandler = Handler()

    suspend fun loadInitCache() = withContext(Dispatchers.Default) {
        val jobs = arrayOf(launch { TermDateInfo.getInfo() },
            launch { PersonalInfo.getInfo() },
            launch { CardBalanceInfo.getInfo() },
            launch { NewsInfo.getInfo(AppPref.readShowNewsType()) },
            launch { CourseInfo.getInfo(CourseInfo.OperationType.INIT_DATA) })
        for (job in jobs) job.join()
    }

    @Synchronized
    fun startCacheCleanerTimer() {
        tryCancelCacheCleaner()
        cacheHandler.postDelayed({
            clearUnusedRunTimeCache()
        }, CACHE_CLEAN_TIMEOUT)
    }

    @Synchronized
    fun tryCancelCacheCleaner() = cacheHandler.removeCallbacksAndMessages(null)

    private fun clearUnusedRunTimeCache() {
        ExamInfo.clearCacheInfo()
        LevelExamInfo.clearCacheInfo()
        MyCourseHistoryInfo.clearCacheInfo()
        MyCourseInfo.clearCacheInfo()
        PersonalInfo.clearCacheInfo()
        System.gc()
    }
}