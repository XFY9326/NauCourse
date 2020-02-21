package tool.xfy9326.naucourses.io.prefs.base

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BasePref {
    companion object {
        @Volatile
        private lateinit var context: WeakReference<Context>

        fun initContext(context: Context) {
            this.context = WeakReference(context)
        }
    }

    protected val pref: SharedPreferences by lazy {
        if (prefName == null) {
            PreferenceManager.getDefaultSharedPreferences(context.get()!!)
        } else {
            context.get()!!.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        }
    }

    fun contains(key: String) = pref.contains(key)

    fun remove(key: String) = pref.edit().remove(key).apply()

    fun clear() = pref.edit().clear().apply()

    protected open val prefName: String? = null

    private inline fun <T> SharedPreferences.delegate(
        key: String? = null, defaultValue: T,
        crossinline getter: SharedPreferences.(String, T) -> T, crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
    ): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T = getter(key ?: property.name, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = edit().setter(key ?: property.name, value).apply()
    }

    fun SharedPreferences.int(key: String? = null, defValue: Int): ReadWriteProperty<Any, Int> =
        delegate(key, defValue, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

    fun SharedPreferences.long(key: String? = null, defValue: Long): ReadWriteProperty<Any, Long> =
        delegate(key, defValue, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

    fun SharedPreferences.float(key: String? = null, defValue: Float): ReadWriteProperty<Any, Float> =
        delegate(key, defValue, SharedPreferences::getFloat, SharedPreferences.Editor::putFloat)

    fun SharedPreferences.boolean(key: String? = null, defValue: Boolean): ReadWriteProperty<Any, Boolean> =
        delegate(key, defValue, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

    fun SharedPreferences.stringSet(key: String? = null, defValue: Set<String>? = null): ReadWriteProperty<Any, Set<String>?> =
        delegate(key, defValue, SharedPreferences::getStringSet, SharedPreferences.Editor::putStringSet)

    fun SharedPreferences.string(key: String? = null, defValue: String? = null): ReadWriteProperty<Any, String?> =
        delegate(key, defValue, SharedPreferences::getString, SharedPreferences.Editor::putString)

}