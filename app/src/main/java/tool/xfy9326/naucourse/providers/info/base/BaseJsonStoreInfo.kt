package tool.xfy9326.naucourse.providers.info.base

import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore

abstract class BaseJsonStoreInfo<T : Any, P : Enum<*>> : BaseSimpleContentInfo<T, P>() {
    protected abstract val jsonStore: BaseJsonStore<T>

    override fun loadSimpleStoredInfo(): T? = jsonStore.loadStore()

    override fun saveSimpleInfo(info: T) {
        jsonStore.saveStore(info)
    }

    override fun clearSimpleStoredInfo() = jsonStore.clearStore()
}