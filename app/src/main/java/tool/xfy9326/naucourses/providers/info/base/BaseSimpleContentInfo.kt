package tool.xfy9326.naucourses.providers.info.base

import tool.xfy9326.naucourses.providers.contents.base.ContentResult

abstract class BaseSimpleContentInfo<T : Any, P : Enum<*>> : BaseContentInfo<BaseSimpleContentInfo.SimpleType, P>() {
    enum class SimpleType {
        DEFAULT
    }

    final override fun loadStoredInfo(): Map<SimpleType, Any> = loadSimpleStoredInfo().let {
        if (it != null || (it is Collection<*> && (it as Collection<*>).size > 0)) {
            mapOf(SimpleType.DEFAULT to loadSimpleStoredInfo() as Any)
        } else {
            emptyMap()
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onReadCache(data: Any): Any = onReadSimpleCache(data as T)

    final override suspend fun getInfoContent(type: SimpleType, params: Set<P>): ContentResult<*> = getSimpleInfoContent(params)

    @Suppress("UNCHECKED_CAST")
    final override fun saveInfo(type: SimpleType, info: Any) = saveSimpleInfo(info as T)

    final override fun clearStoredInfo(type: SimpleType) = clearSimpleStoredInfo()

    final override fun getCachedItem(type: SimpleType): Any? = super.getCachedItem(type)

    final override fun hasCachedItem(type: SimpleType): Boolean = super.hasCachedItem(type)

    @Suppress("UNCHECKED_CAST")
    final override fun <E : Any> onSaveResult(type: SimpleType, params: Set<P>, data: E) = onSaveSimpleResult(params, data as T)

    @Suppress("UNCHECKED_CAST")
    final override fun <E : Any> onSaveCache(type: SimpleType, params: Set<P>, data: E) = onSaveSimpleCache(params, data as T)

    final override fun <E : Any> updateCache(type: SimpleType, data: E) = super.updateCache(type, data)

    final override fun isCacheExpired(type: SimpleType, params: Set<P>, cacheExpire: CacheExpire): Boolean = isSimpleCacheExpired(params, cacheExpire)


    suspend fun getInfo(params: P, loadCache: Boolean = false, forceRefresh: Boolean = false): InfoResult<T> =
        getInfo(setOf(params), loadCache, forceRefresh)

    suspend fun getInfo(params: Set<P> = emptySet(), loadCache: Boolean = false, forceRefresh: Boolean = false): InfoResult<T> =
        getInfoProcess(SimpleType.DEFAULT, params, loadCache, forceRefresh)


    protected open fun onReadSimpleCache(data: T): T = data

    protected abstract fun loadSimpleStoredInfo(): T?

    protected abstract suspend fun getSimpleInfoContent(params: Set<P>): ContentResult<T>

    protected abstract fun saveSimpleInfo(info: T)

    protected abstract fun clearSimpleStoredInfo()

    @Suppress("UNCHECKED_CAST")
    protected fun getSimpleCachedItem(): T? = super.getCachedItem(SimpleType.DEFAULT) as T?

    protected fun hasSimpleCachedItem(): Boolean = super.hasCachedItem(SimpleType.DEFAULT)

    protected open fun onSaveSimpleResult(params: Set<P>, data: T) = super.onSaveResult(SimpleType.DEFAULT, params, data)

    protected open fun onSaveSimpleCache(params: Set<P>, data: T) = super.onSaveCache(SimpleType.DEFAULT, params, data)

    protected fun updateSimpleCache(data: T) = super.updateCache(SimpleType.DEFAULT, data)

    protected open fun isSimpleCacheExpired(params: Set<P>, cacheExpire: CacheExpire): Boolean =
        super.isCacheExpired(SimpleType.DEFAULT, params, cacheExpire)
}