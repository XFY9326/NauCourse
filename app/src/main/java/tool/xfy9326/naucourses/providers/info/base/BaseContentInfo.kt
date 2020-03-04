package tool.xfy9326.naucourses.providers.info.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.io.prefs.InfoStoredTimePref
import tool.xfy9326.naucourses.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import java.util.*

abstract class BaseContentInfo<T : Enum<*>, P : Enum<*>> {
    private val cacheMap = Hashtable<T, Any>(1)
    private var hasInit = false
    private val infoMutex = Mutex()

    companion object {
        private const val KEY_JOIN_SYMBOL = "_"
    }

    private fun initCache() {
        val stored = loadStoredInfo()
        cacheMap.clear()
        cacheMap.putAll(stored)
        hasInit = true
    }

    protected open fun <E : Any> updateCache(type: T, data: E) {
        cacheMap[type] = data
    }

    protected open fun onGetCacheExpire(): CacheExpire = CacheExpire()

    @Suppress("UNCHECKED_CAST")
    protected open fun <E : Any> onSaveResult(type: T, params: Set<P>, data: E) {
        saveInfo(type, data)
        InfoStoredTimePref.saveStoredTime(getKeyName(type), System.currentTimeMillis())
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun <E : Any> onSaveCache(type: T, params: Set<P>, data: E) {
        cacheMap[type] = data
    }

    protected open fun onReadCache(data: Any): Any = data

    protected abstract fun loadStoredInfo(): Map<T, Any>

    protected abstract suspend fun getInfoContent(type: T, params: Set<P>): ContentResult<*>

    protected abstract fun saveInfo(type: T, info: Any)

    abstract fun clearStoredInfo(type: T)

    protected open fun hasCachedItem(type: T): Boolean {
        synchronized(this) {
            if (!hasInit) {
                initCache()
            }
        }
        return cacheMap.containsKey(type) && cacheMap[type] != null
    }

    protected open fun getCachedItem(type: T): Any? {
        synchronized(this) {
            if (!hasInit) {
                initCache()
            }
        }
        return cacheMap[type]
    }

    @Synchronized
    protected open fun isCacheExpired(type: T, params: Set<P>, cacheExpire: CacheExpire): Boolean {
        return when (cacheExpire.expireRule) {
            CacheExpireRule.INSTANTLY -> true
            CacheExpireRule.PER_TIME ->
                System.currentTimeMillis() > (cacheExpire.expireTimeUnit.getTimeInTimeUnit(InfoStoredTimePref.loadStoredTime(getKeyName(type))) +
                        cacheExpire.expireTimeUnit.toMillis(cacheExpire.expireTime))
            CacheExpireRule.AFTER_TIME -> System.currentTimeMillis() > (cacheExpire.expireTimeUnit.toMillis(cacheExpire.expireTime) +
                    InfoStoredTimePref.loadStoredTime(getKeyName(type)))
        }
    }

    private fun getKeyName(type: T) = "${this.javaClass.simpleName}$KEY_JOIN_SYMBOL${type.name}"

    @Suppress("UNCHECKED_CAST")
    protected suspend fun <E : Any> getInfoProcess(
        type: T,
        params: Set<P> = emptySet(),
        loadCachedData: Boolean = false,
        forceRefresh: Boolean = false
    ): InfoResult<E> = withContext(Dispatchers.Default) {
        if (loadCachedData && forceRefresh) {
            throw IllegalArgumentException("You Can't Do Load Cache And Refresh At The Same Time!")
        }
        infoMutex.withLock {
            val hasCachedData = hasCachedItem(type)
            val cacheExpire = onGetCacheExpire()
            if (loadCachedData) {
                if (hasCachedData) {
                    InfoResult(true, onReadCache(getCachedItem(type)!!) as E)
                } else {
                    InfoResult(false, errorReason = ContentErrorReason.EMPTY_DATA)
                }
            } else if (!forceRefresh && hasCachedData && !isCacheExpired(type, params, cacheExpire)) {
                InfoResult(true, onReadCache(getCachedItem(type)!!) as E)
            } else {
                val result = getInfoContent(type, params)
                if (result.isSuccess) {
                    onSaveResult(type, params, result.contentData!! as E)
                    onSaveCache(type, params, result.contentData as E)
                    InfoResult(true, result.contentData)
                } else {
                    InfoResult(false, errorReason = result.contentErrorResult)
                }
            }
        }
    }

    @Synchronized
    fun clearCacheInfo() = cacheMap.clear()
}