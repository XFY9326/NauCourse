package tool.xfy9326.naucourse.providers

import kotlinx.coroutines.*
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.info.methods.*
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType

// 全局缓存管理
object GlobalCacheManager {
    private const val CACHE_CLEAN_TIMEOUT = 5000L
    private var cacheJob: Job? = null

    suspend fun loadInitCache() = withContext(Dispatchers.IO) {
        joinAll(
            launch { TermDateInfo.getInfo() },
            launch { PersonalInfo.getInfo() },
            launch { CardBalanceInfo.getInfo() },
            launch { NewsInfo.getInfo(AppPref.readShowNewsType()) },
            launch {
                CourseInfo.getInfo(CourseInfo.OperationType.INIT_DATA).apply {
                    if (!isSuccess && errorReason == ContentErrorReason.DATA_ERROR) {
                        NotifyBus[NotifyType.COURSE_INIT_CONFLICT].notifyEvent()
                    }
                }
            })
    }

    // 后台时间长时，清空部分cache数据
    @Synchronized
    fun startCacheCleanerTimer() {
        tryCancelCacheCleaner()
        cacheJob = GlobalScope.launch(Dispatchers.Default) {
            delay(CACHE_CLEAN_TIMEOUT)

            ExamInfo.clearCacheInfo()
            LevelExamInfo.clearCacheInfo()
            MyCourseHistoryInfo.clearCacheInfo()
            MyCourseInfo.clearCacheInfo()
            PersonalInfo.clearCacheInfo()
        }
    }

    @Synchronized
    fun tryCancelCacheCleaner() = cacheJob?.cancel()
}