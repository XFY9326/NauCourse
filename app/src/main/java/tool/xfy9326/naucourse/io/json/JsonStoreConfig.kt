package tool.xfy9326.naucourse.io.json

interface JsonStoreConfig<T : Any> {
    val fileName: String
    val versionCode: Int
    val storeClass: Class<T>
}