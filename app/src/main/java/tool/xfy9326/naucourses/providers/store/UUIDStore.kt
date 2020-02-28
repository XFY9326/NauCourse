package tool.xfy9326.naucourses.providers.store

import tool.xfy9326.naucourses.beans.UUIDContent
import tool.xfy9326.naucourses.io.json.GsonStoreManager
import tool.xfy9326.naucourses.providers.store.base.BaseGsonStore

object UUIDStore : BaseGsonStore<UUIDContent>() {
    override val useCache: Boolean = true
    // 由于使用该值进行加密，所以此处不能被加密，否则会进入死循环
    override val useEncrypt: Boolean = false
    override val storeType: GsonStoreManager.StoreType = GsonStoreManager.StoreType.UUID
}