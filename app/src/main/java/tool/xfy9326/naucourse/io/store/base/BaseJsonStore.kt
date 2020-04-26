package tool.xfy9326.naucourse.io.store.base

import java.util.*

// 基础JSON存储
abstract class BaseJsonStore<T : Any> : JsonStoreConfig<T> {
    protected abstract val useCache: Boolean
    protected abstract val useEncrypt: Boolean

    @Volatile
    private var hasInit = false

    @Volatile
    private var cache: T? = null

    private var listenerList: Vector<OnStoreChangedListener<T>> = Vector()

    private fun initStore() {
        if (useCache) {
            cache = JsonStoreManager.readData(this, useEncrypt)
            listenerList.forEach {
                it.onInitLoad()
            }
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
            JsonStoreManager.readData(this, useEncrypt)
        }

    fun saveStore(data: T): Boolean {
        if (useCache) {
            this.cache = data
            hasInit = true
        }
        return JsonStoreManager.writeData(this, data, useEncrypt).also {
            listenerList.forEach {
                it.onDataChanged(data)
            }
        }
    }

    fun clearStore() {
        if (useCache) {
            this.cache = null
        }
        JsonStoreManager.clearData(this)
        listenerList.forEach {
            it.onClear()
        }
    }

    @Suppress("unused")
    fun addListener(listener: OnStoreChangedListener<T>, receiveNowData: Boolean = false) {
        listenerList.add(listener)
        if (receiveNowData) {
            loadStore()?.let {
                listener.onDataChanged(it)
            }
        }
    }

    @Suppress("unused")
    fun removeListener(listener: OnStoreChangedListener<T>) {
        listenerList.remove(listener)
    }

    interface OnStoreChangedListener<T : Any> {
        fun onInitLoad()

        fun onDataChanged(data: T)

        fun onClear()
    }
}