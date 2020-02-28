package tool.xfy9326.naucourses.providers.store.base

import tool.xfy9326.naucourses.io.json.GsonStoreManager
import tool.xfy9326.naucourses.io.json.GsonStoreType

abstract class BaseGsonStore<T : Any> {
    protected abstract val storeType: GsonStoreType
    protected abstract val useCache: Boolean
    protected abstract val useEncrypt: Boolean
    private var hasInit = false
    private var cache: T? = null

    private fun initStore() {
        if (useCache) {
            cache = GsonStoreManager.readData(storeType, useEncrypt)
        }
        hasInit = true
    }

    fun loadStore(): T? =
        if (useCache) {
            synchronized(this) {
                if (!hasInit) {
                    initStore()
                }
            }
            cache
        } else {
            GsonStoreManager.readData(storeType, useEncrypt)
        }

    fun saveStore(data: T): Boolean {
        if (useCache) {
            this.cache = data
        }
        return GsonStoreManager.writeData(storeType, data, useEncrypt)
    }

    fun clearStore() {
        if (useCache) {
            this.cache = null
        }
        GsonStoreManager.clearData(storeType)
    }
}