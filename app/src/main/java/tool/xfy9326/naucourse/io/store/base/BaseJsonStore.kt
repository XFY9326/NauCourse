package tool.xfy9326.naucourse.io.store.base

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// 基础JSON存储
abstract class BaseJsonStore<T : Any> : JsonStoreConfig<T> {
    protected abstract val useCache: Boolean
    protected abstract val useEncrypt: Boolean
    private val storeMutex = Mutex()

    @Volatile
    private var hasInit = false

    @Volatile
    private var cache: T? = null

    private suspend fun initStore() {
        if (useCache) {
            cache = JsonStoreManager.readData(this, useEncrypt)
        }
        hasInit = true
    }

    suspend fun loadStore(): T? =
        if (useCache) {
            storeMutex.withLock {
                if (!hasInit) {
                    initStore()
                }
            }
            cache
        } else {
            JsonStoreManager.readData(this, useEncrypt)
        }

    suspend fun saveStore(data: T): Boolean {
        if (useCache) {
            this.cache = data
            hasInit = true
        }
        return JsonStoreManager.writeData(this, data, useEncrypt)
    }

    fun clearStore() {
        if (useCache) {
            this.cache = null
        }
        JsonStoreManager.clearData(this)
    }
}