package tool.xfy9326.naucourses.io.prefs.base

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.utils.secure.CryptoUtils
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BasePref {
    protected val pref: SharedPreferences by lazy {
        if (prefName == null) {
            PreferenceManager.getDefaultSharedPreferences(App.instance)
        } else {
            App.instance.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        }
    }

    fun contains(key: String) = pref.contains(key)

    fun remove(key: String) = pref.edit().remove(key).apply()

    fun clear() = pref.edit().clear().apply()

    protected open val prefName: String? = null

    private inline fun <T> SharedPreferences.delegate(
        key: String? = null, defaultValue: T, encrypted: Boolean,
        crossinline getter: SharedPreferences.(String, T) -> T, crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
    ): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            val readValue = getter(key ?: property.name, defaultValue)
            return if (!encrypted || readValue == defaultValue) {
                readValue
            } else {
                if (readValue is String) {
                    // 仅限字符串加密
                    @Suppress("UNCHECKED_CAST")
                    CryptoUtils.decryptText(readValue) as T
                } else {
                    readValue
                }
            }
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            val saveValue = if (value is String && encrypted) {
                // 仅限字符串加密
                @Suppress("UNCHECKED_CAST")
                CryptoUtils.encryptText(value) as T
            } else {
                value
            }
            edit().setter(key ?: property.name, saveValue).apply()
        }
    }

    fun SharedPreferences.int(key: String? = null, defValue: Int): ReadWriteProperty<Any, Int> =
        delegate(key, defValue, false, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

    fun SharedPreferences.long(key: String? = null, defValue: Long): ReadWriteProperty<Any, Long> =
        delegate(key, defValue, false, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

    fun SharedPreferences.float(key: String? = null, defValue: Float): ReadWriteProperty<Any, Float> =
        delegate(key, defValue, false, SharedPreferences::getFloat, SharedPreferences.Editor::putFloat)

    fun SharedPreferences.boolean(key: String? = null, defValue: Boolean): ReadWriteProperty<Any, Boolean> =
        delegate(key, defValue, false, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

    fun SharedPreferences.stringSet(key: String? = null, defValue: Set<String>? = null): ReadWriteProperty<Any, Set<String>?> =
        delegate(key, defValue, false, SharedPreferences::getStringSet, SharedPreferences.Editor::putStringSet)

    fun SharedPreferences.string(key: String? = null, defValue: String? = null, encrypted: Boolean = false): ReadWriteProperty<Any, String?> =
        delegate(key, defValue, encrypted, SharedPreferences::getString, SharedPreferences.Editor::putString)

}