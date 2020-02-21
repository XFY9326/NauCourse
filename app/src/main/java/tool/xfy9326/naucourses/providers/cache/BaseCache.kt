package tool.xfy9326.naucourses.providers.cache

import tool.xfy9326.naucourses.io.json.GsonStoreManager

abstract class BaseCache<T : Any> {
    protected abstract val cacheType: GsonStoreManager.StoreType
    private var cache: T? = null
    private val storeManager = GsonStoreManager.getInstance()

    fun initCache() {
        cache = storeManager.readData(cacheType)
    }

    fun loadCache(): T? = cache

    fun saveCache(cache: T): Boolean {
        this.cache = cache
        return storeManager.writeData(cacheType, cache)
    }

    fun clearCache() {
        this.cache = null
        storeManager.clearData(cacheType)
    }
}