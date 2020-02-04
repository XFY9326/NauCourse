package tool.xfy9326.naucourses.io.configs.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import tool.xfy9326.naucourses.io.configs.base.BaseAdapter
import tool.xfy9326.naucourses.io.configs.base.BaseGroup
import tool.xfy9326.naucourses.io.configs.base.BaseKey

class PreferenceAdapter(context: Context, prefName: String? = null) : BaseAdapter {
    private val sharedPreference: SharedPreferences by lazy {
        if (prefName == null) {
            PreferenceManager.getDefaultSharedPreferences(context)
        } else {
            context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> read(key: BaseKey<T>, defaultValue: T): T = with(sharedPreference) {
        (when (defaultValue) {
            is String -> getString(key.keyValue, defaultValue)
            is Int -> getInt(key.keyValue, defaultValue)
            is Boolean -> getBoolean(key.keyValue, defaultValue)
            is Long -> getLong(key.keyValue, defaultValue)
            is Float -> getFloat(key.keyValue, defaultValue)
            is Set<*> -> {
                val cast = try {
                    defaultValue as Set<String>
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException("Unsupported Read Type")
                }
                getStringSet(key.keyValue, cast)
            }
            else -> throw IllegalArgumentException("Unsupported Read Type")
        } ?: defaultValue) as T
    }

    override fun read(group: BaseGroup): Map<String, Any> {
        var data: List<String>
        val map: HashMap<String, Any> = HashMap()
        for (mutableEntry in sharedPreference.all) {
            data = mutableEntry.key.split(BaseKey.PARAM_JOIN_SYMBOL)
            if (data[0] == group.getValue() && mutableEntry.value != null) {
                map[data[1]] = mutableEntry.value!!
            }
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> write(key: BaseKey<T>, value: T) = with(sharedPreference.edit()) {
        when (value) {
            is String -> putString(key.keyValue, value)
            is Int -> putInt(key.keyValue, value)
            is Boolean -> putBoolean(key.keyValue, value)
            is Long -> putLong(key.keyValue, value)
            is Float -> putFloat(key.keyValue, value)
            is Set<*> -> {
                val cast = try {
                    value as Set<String>
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException("Unsupported Write Type")
                }
                putStringSet(key.keyValue, cast)
            }
            else -> throw IllegalArgumentException("Unsupported Write Type")
        }.apply()
    }

    override fun <T : Any> contains(key: BaseKey<T>): Boolean = sharedPreference.contains(key.keyValue)

    override fun <T : Any> delete(key: BaseKey<T>) = sharedPreference.edit().remove(key.keyValue).apply()

    override fun clearAll() = sharedPreference.edit().clear().apply()

    override fun clearAllByGroup(group: BaseGroup) = with(sharedPreference.edit()) {
        var data: List<String>
        for (mutableEntry in sharedPreference.all) {
            data = mutableEntry.key.split(BaseKey.PARAM_JOIN_SYMBOL)
            if (data[0] == group.getValue()) {
                remove(mutableEntry.key)
            }
        }
    }
}