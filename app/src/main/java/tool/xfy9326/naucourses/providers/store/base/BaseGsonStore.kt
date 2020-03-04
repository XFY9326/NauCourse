package tool.xfy9326.naucourses.providers.store.base

import tool.xfy9326.naucourses.io.json.GsonStoreManager
import tool.xfy9326.naucourses.io.json.GsonStoreType
import java.util.*

abstract class BaseGsonStore<T : Any> {
    protected abstract val storeType: GsonStoreType
    protected abstract val useCache: Boolean
    protected abstract val useEncrypt: Boolean
    private var hasInit = false
    private var cache: T? = null
    private var listenerList: Vector<OnStoreChangedListener<T>> = Vector()

    private fun initStore() {
        if (useCache) {
            cache = GsonStoreManager.readData(storeType, useEncrypt)
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
            GsonStoreManager.readData(storeType, useEncrypt)
        }

    fun saveStore(data: T): Boolean {
        if (useCache) {
            this.cache = data
        }
        return GsonStoreManager.writeData(storeType, data, useEncrypt).also {
            listenerList.forEach {
                it.onDataChanged(data)
            }
        }
    }

    fun clearStore() {
        if (useCache) {
            this.cache = null
        }
        GsonStoreManager.clearData(storeType)
        listenerList.forEach {
            it.onClear()
        }
    }

    fun addListener(listener: OnStoreChangedListener<T>, receiveNowData: Boolean = false) {
        listenerList.add(listener)
        if (receiveNowData) {
            loadStore()?.let {
                listener.onDataChanged(it)
            }
        }
    }

    fun removeListener(listener: OnStoreChangedListener<T>) {
        listenerList.remove(listener)
    }

    interface OnStoreChangedListener<T : Any> {
        fun onInitLoad()

        fun onDataChanged(data: T)

        fun onClear()
    }
}