package tool.xfy9326.naucourse.providers.info.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.prefs.InfoStoredTimePref
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import java.util.*

abstract class BaseContentInfo<Type : Enum<*>, Param> {
    private val cacheMap = Hashtable<Type, Any>(1)

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

    protected open fun <Element : Any> updateCache(type: Type, data: Element) {
        cacheMap[type] = data
        hasInit = true
    }

    protected open fun onGetCacheExpire(): CacheExpire = CacheExpire()

    @Suppress("UNCHECKED_CAST")
    protected open fun <E : Any> onSaveResult(type: Type, params: Set<Param>, data: E) {
        saveInfo(type, data)
        InfoStoredTimePref.saveStoredTime(getKeyName(type), System.currentTimeMillis())
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun <E : Any> onSaveCache(type: Type, params: Set<Param>, data: E) {
        cacheMap[type] = data
    }

    protected open fun onReadCache(data: Any): Any = data

    protected abstract fun loadStoredInfo(): Map<Type, Any>

    protected abstract suspend fun getInfoContent(type: Type, params: Set<Param>): ContentResult<*>

    protected abstract fun saveInfo(type: Type, info: Any)

    abstract fun clearStoredInfo(type: Type)

    protected open fun hasCachedItem(type: Type): Boolean {
        synchronized(this) {
            if (!hasInit) {
                initCache()
            }
        }
        return cacheMap.containsKey(type) && cacheMap[type] != null
    }

    protected open fun getCachedItem(type: Type): Any? {
        synchronized(this) {
            if (!hasInit) {
                initCache()
            }
        }
        return cacheMap[type]
    }

    @Synchronized
    protected open fun isCacheExpired(type: Type, params: Set<Param>, cacheExpire: CacheExpire): Boolean {
        return when (cacheExpire.expireRule) {
            CacheExpireRule.INSTANTLY -> true
            CacheExpireRule.PER_TIME ->
                System.currentTimeMillis() > (cacheExpire.expireTimeUnit.getTimeInTimeUnit(InfoStoredTimePref.loadStoredTime(getKeyName(type))) +
                        cacheExpire.expireTimeUnit.toMillis(cacheExpire.expireTime))
            CacheExpireRule.AFTER_TIME -> System.currentTimeMillis() > (cacheExpire.expireTimeUnit.toMillis(cacheExpire.expireTime) +
                    InfoStoredTimePref.loadStoredTime(getKeyName(type)))
        }
    }

    private fun getKeyName(type: Type) = "${this.javaClass.simpleName}$KEY_JOIN_SYMBOL${type.name}"

    @Suppress("UNCHECKED_CAST")
    protected suspend fun <E : Any> getInfoProcess(
        type: Type,
        params: Set<Param> = emptySet(),
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
                    if (cacheExpire.expireRule != CacheExpireRule.INSTANTLY) {
                        onSaveCache(type, params, result.contentData as E)
                    }
                    return@withContext InfoResult(true, result.contentData as E)
                } else {
                    return@withContext InfoResult<E>(false, errorReason = result.contentErrorResult)
                }
            }
        }
    }

    fun clearCacheInfo() = synchronized(this) {
        hasInit = false
        cacheMap.clear()
    }
}