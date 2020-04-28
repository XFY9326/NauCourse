package tool.xfy9326.naucourse.providers.info.base

import tool.xfy9326.naucourse.providers.contents.base.ContentResult

abstract class BaseSimpleContentInfo<Element : Any, Param> : BaseContentInfo<BaseSimpleContentInfo.SimpleType, Param>() {
    enum class SimpleType {
        DEFAULT
    }

    final override suspend fun loadStoredInfo(): Map<SimpleType, Any> = loadSimpleStoredInfo().let {
        if (it != null || (it is Collection<*> && (it as Collection<*>).size > 0)) {
            mapOf(SimpleType.DEFAULT to loadSimpleStoredInfo() as Any)
        } else {
            emptyMap()
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onReadCache(data: Any): Any = onReadSimpleCache(data as Element)

    final override suspend fun getInfoContent(type: SimpleType, params: Set<Param>): ContentResult<*> = getSimpleInfoContent(params)

    @Suppress("UNCHECKED_CAST")
    final override suspend fun saveInfo(type: SimpleType, info: Any) = saveSimpleInfo(info as Element)

    final override suspend fun clearStoredInfo(type: SimpleType) = clearSimpleStoredInfo()

    final override suspend fun getCachedItem(type: SimpleType): Any? = super.getCachedItem(type)

    final override suspend fun hasCachedItem(type: SimpleType): Boolean = super.hasCachedItem(type)

    @Suppress("UNCHECKED_CAST")
    final override suspend fun <E : Any> onSaveResult(type: SimpleType, params: Set<Param>, data: E) = onSaveSimpleResult(params, data as Element)

    @Suppress("UNCHECKED_CAST")
    final override fun <E : Any> onSaveCache(type: SimpleType, params: Set<Param>, data: E) = onSaveSimpleCache(params, data as Element)

    final override fun <E : Any> updateCache(type: SimpleType, data: E) = super.updateCache(type, data)

    final override fun isCacheExpired(type: SimpleType, params: Set<Param>, cacheExpire: CacheExpire): Boolean =
        isSimpleCacheExpired(params, cacheExpire)


    suspend fun getInfo(params: Param, loadCache: Boolean = false, forceRefresh: Boolean = false): InfoResult<Element> =
        getInfo(setOf(params), loadCache, forceRefresh)

    open suspend fun getInfo(params: Set<Param> = emptySet(), loadCache: Boolean = false, forceRefresh: Boolean = false): InfoResult<Element> =
        getInfoProcess(SimpleType.DEFAULT, params, loadCache, forceRefresh)

    fun isCacheExpired(params: Set<Param> = emptySet()) =
        isCacheExpired(SimpleType.DEFAULT, params, onGetCacheExpire())


    protected open fun onReadSimpleCache(data: Element): Element = data

    protected abstract suspend fun loadSimpleStoredInfo(): Element?

    protected abstract suspend fun getSimpleInfoContent(params: Set<Param>): ContentResult<Element>

    protected abstract suspend fun saveSimpleInfo(info: Element)

    protected abstract suspend fun clearSimpleStoredInfo()

    @Suppress("UNCHECKED_CAST")
    protected suspend fun getSimpleCachedItem(): Element? = super.getCachedItem(SimpleType.DEFAULT) as Element?

    protected suspend fun hasSimpleCachedItem(): Boolean = super.hasCachedItem(SimpleType.DEFAULT)

    protected open suspend fun onSaveSimpleResult(params: Set<Param>, data: Element) = super.onSaveResult(SimpleType.DEFAULT, params, data)

    protected open fun onSaveSimpleCache(params: Set<Param>, data: Element) = super.onSaveCache(SimpleType.DEFAULT, params, data)

    protected fun updateSimpleCache(data: Element) = super.updateCache(SimpleType.DEFAULT, data)

    protected open fun isSimpleCacheExpired(params: Set<Param>, cacheExpire: CacheExpire): Boolean =
        super.isCacheExpired(SimpleType.DEFAULT, params, cacheExpire)
}