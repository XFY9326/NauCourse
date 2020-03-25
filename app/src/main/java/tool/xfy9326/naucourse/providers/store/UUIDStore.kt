package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.beans.UUIDContent
import tool.xfy9326.naucourse.io.gson.GsonStoreType
import tool.xfy9326.naucourse.providers.store.base.BaseGsonStore

object UUIDStore : BaseGsonStore<UUIDContent>() {
    override val useCache: Boolean = true

    // 由于使用该值进行加密，所以此处不能被加密，否则会进入死循环
    override val useEncrypt: Boolean = false
    override val storeType: GsonStoreType = GsonStoreType.UUID
}