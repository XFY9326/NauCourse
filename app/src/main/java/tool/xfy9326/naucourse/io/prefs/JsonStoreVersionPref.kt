package tool.xfy9326.naucourse.io.prefs

import tool.xfy9326.naucourse.io.prefs.base.BasePref

object JsonStoreVersionPref : BasePref() {
    override val prefName: String = "JsonStoreVersion"

    fun saveStoredVersion(key: String, storedVersion: Int) = pref.edit().putInt(key, storedVersion).apply()

    fun loadStoredVersion(key: String): Int = pref.getInt(key, 0)
}