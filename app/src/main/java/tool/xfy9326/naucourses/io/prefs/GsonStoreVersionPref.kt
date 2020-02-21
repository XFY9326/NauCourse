package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.io.prefs.base.BasePref

object GsonStoreVersionPref : BasePref() {
    override val prefName: String = "JsonStoreVersion"

    fun saveStoredVersion(key: String, storedVersion: Int) = pref.edit().putInt(key, storedVersion).apply()

    fun loadStoredVersion(key: String): Int = pref.getInt(key, 0)
}