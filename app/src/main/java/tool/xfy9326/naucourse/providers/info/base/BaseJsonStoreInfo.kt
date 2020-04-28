package tool.xfy9326.naucourse.providers.info.base

import tool.xfy9326.naucourse.io.store.base.BaseJsonStore

abstract class BaseJsonStoreInfo<T : Any, P : Enum<*>> : BaseSimpleContentInfo<T, P>() {
    protected abstract val jsonStore: BaseJsonStore<T>

    override suspend fun loadSimpleStoredInfo(): T? = jsonStore.loadStore()

    override suspend fun saveSimpleInfo(info: T) {
        jsonStore.saveStore(info)
    }

    override suspend fun clearSimpleStoredInfo() = jsonStore.clearStore()
}