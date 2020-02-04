package tool.xfy9326.naucourses.io.configs.base

interface BaseAdapter {
    fun <T : Any> read(key: BaseKey<T>): T = read(key, key.defaultValue)

    fun <T : Any> read(key: BaseKey<T>, defaultValue: T): T

    fun read(group: BaseGroup): Map<String, Any>

    fun <T : Any> write(key: BaseKey<T>, value: T)

    fun clearAll()

    fun clearAllByGroup(group: BaseGroup)

    fun <T : Any> delete(key: BaseKey<T>)

    fun <T : Any> contains(key: BaseKey<T>): Boolean
}