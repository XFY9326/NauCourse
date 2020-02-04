package tool.xfy9326.naucourses.io.configs.utils

import tool.xfy9326.naucourses.io.configs.base.BaseAdapter
import tool.xfy9326.naucourses.io.configs.base.BaseGroup
import tool.xfy9326.naucourses.io.configs.base.BaseKey
import java.util.*

object RuntimeAdapter : BaseAdapter {
    private val table: Hashtable<BaseKey<*>, Any> by lazy { Hashtable<BaseKey<*>, Any>() }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> read(key: BaseKey<T>, defaultValue: T): T = (table[key] ?: defaultValue) as T

    override fun read(group: BaseGroup): Map<String, Any> {
        val map: HashMap<String, Any> = HashMap()
        for (mutableEntry in table) {
            if (group == mutableEntry.key.group) {
                map[mutableEntry.key.itemName] = mutableEntry.value
            }
        }
        return map
    }

    override fun <T : Any> write(key: BaseKey<T>, value: T) {
        table[key] = value
    }

    override fun <T : Any> contains(key: BaseKey<T>): Boolean = table.contains(key)

    override fun <T : Any> delete(key: BaseKey<T>) {
        table.remove(key)
    }

    override fun clearAll() = table.clear()

    override fun clearAllByGroup(group: BaseGroup) {
        for (mutableEntry in table) {
            if (group == mutableEntry.key.group) {
                table.remove(mutableEntry.key)
            }
        }
    }
}