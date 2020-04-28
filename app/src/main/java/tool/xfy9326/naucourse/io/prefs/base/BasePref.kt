package tool.xfy9326.naucourse.io.prefs.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import tool.xfy9326.naucourse.App
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

    fun clear() = pref.edit().clear().commit()

    open val prefName: String? = null

    private inline fun <T> SharedPreferences.delegate(
        key: String? = null,
        defaultValue: T,
        commit: Boolean = false,
        crossinline getter: SharedPreferences.(String, T) -> T,
        crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
    ): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>) = getter(key ?: property.name, defaultValue)

        @SuppressLint("ApplySharedPref")
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            edit().setter(key ?: property.name, value).apply {
                if (commit) commit() else apply()
            }
        }
    }

    fun SharedPreferences.int(key: String? = null, defValue: Int, commit: Boolean = false): ReadWriteProperty<Any, Int> =
        delegate(key, defValue, commit, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

    fun SharedPreferences.long(key: String? = null, defValue: Long, commit: Boolean = false): ReadWriteProperty<Any, Long> =
        delegate(key, defValue, commit, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

    fun SharedPreferences.float(key: String? = null, defValue: Float, commit: Boolean = false): ReadWriteProperty<Any, Float> =
        delegate(key, defValue, commit, SharedPreferences::getFloat, SharedPreferences.Editor::putFloat)

    fun SharedPreferences.boolean(key: String? = null, defValue: Boolean, commit: Boolean = false): ReadWriteProperty<Any, Boolean> =
        delegate(key, defValue, commit, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

    fun SharedPreferences.stringSet(
        key: String? = null,
        defValue: Set<String>? = null,
        commit: Boolean = false
    ): ReadWriteProperty<Any, Set<String>?> =
        delegate(key, defValue, commit, SharedPreferences::getStringSet, SharedPreferences.Editor::putStringSet)

    fun SharedPreferences.string(
        key: String? = null,
        defValue: String? = null,
        commit: Boolean = false
    ): ReadWriteProperty<Any, String?> =
        delegate(key, defValue, commit, SharedPreferences::getString, SharedPreferences.Editor::putString)

}