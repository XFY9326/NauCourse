package tool.xfy9326.naucourses.io.configs.base

abstract class BaseConfig<T : BaseAdapter> {
    abstract fun getStoreAdapter(): T

    fun <T : Any> createKey(group: BaseGroup, itemName: String, defaultValue: T): BaseKey<T> {
        return BaseKey(group, itemName, getStoreAdapter(), defaultValue)
    }
}