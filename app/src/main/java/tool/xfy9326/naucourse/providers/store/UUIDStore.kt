package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore

object UUIDStore : BaseJsonStore<UUIDStore.UUIDContent>() {
    override val fileName: String = "UUID"
    override val versionCode: Int = 1
    override val storeClass: Class<UUIDContent> = UUIDContent::class.java

    override val useCache: Boolean = true

    // 由于使用该值进行加密，所以此处不能被加密，否则会进入死循环
    override val useEncrypt: Boolean = false

    fun saveUUID(newUUID: String) {
        UUIDStore.saveStore(UUIDContent(newUUID))
    }

    fun readUUID() = loadStore()?.content

    data class UUIDContent(
        val content: String
    )
}