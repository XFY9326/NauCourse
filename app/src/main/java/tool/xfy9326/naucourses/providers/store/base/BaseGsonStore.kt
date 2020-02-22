package tool.xfy9326.naucourses.providers.store.base

import tool.xfy9326.naucourses.io.json.GsonStoreManager

abstract class BaseGsonStore<T : Any> {
    protected abstract val storeType: GsonStoreManager.StoreType
    protected abstract val useCache: Boolean
    private var cache: T? = null
    private val storeManager = GsonStoreManager.getInstance()

    fun initStore() {
        if (useCache) {
            cache = storeManager.readData(storeType)
        }
    }

    fun loadStore(): T? =
        if (useCache) {
            cache
        } else {
            storeManager.readData(storeType)
        }

    fun saveStore(data: T): Boolean {
        if (useCache) {
            this.cache = data
        }
        return storeManager.writeData(storeType, data)
    }

    fun clearStore() {
        if (useCache) {
            this.cache = null
        }
        storeManager.clearData(storeType)
    }
}