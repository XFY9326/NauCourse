package tool.xfy9326.naucourse.providers.info.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.prefs.InfoStoredTimePref
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import java.util.*

abstract class BaseContentInfo<T : Enum<*>, P : Enum<*>> {
    private val cacheMap = Hashtable<T, Any>(1)

    @Volatile
    private var hasInit = false

    private val infoMutex = Mutex()
    private val cacheMutex = Mutex()

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
        hasInit = true
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

        if (loadCachedData) {
            cacheMutex.withLock {
                if (hasCachedItem(type)) {
                    return@withContext InfoResult(true, onReadCache(getCachedItem(type)!!) as E)
                } else {
                    return@withContext InfoResult<E>(false, errorReason = ContentErrorReason.EMPTY_DATA)
                }
            }
        }

        val cacheExpire = onGetCacheExpire()
        val useCache = !forceRefresh && hasCachedItem(type) && !isCacheExpired(type, params, cacheExpire)
        if (useCache) {
            cacheMutex.withLock {
                return@withContext InfoResult(true, onReadCache(getCachedItem(type)!!) as E)
            }
        } else {
            infoMutex.withLock {
                val result = getInfoContent(type, params)
                if (result.isSuccess) {
                    onSaveResult(type, params, result.contentData!! as E)
                    onSaveCache(type, params, result.contentData as E)
                    return@withContext InfoResult(true, result.contentData)
                } else {
                    return@withContext InfoResult<E>(false, errorReason = result.contentErrorResult)
                }
            }
        }
    }

    fun clearCacheInfo() = synchronized(this) { cacheMap.clear() }
}