package tool.xfy9326.naucourse.io.store.base

interface JsonStoreConfig<T : Any> {
    val fileName: String
    val versionCode: Int
    val storeClass: Class<T>
}