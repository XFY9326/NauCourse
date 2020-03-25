package tool.xfy9326.naucourse.io.prefs

import tool.xfy9326.naucourse.io.prefs.base.BasePref

object InfoStoredTimePref : BasePref() {
    override val prefName: String = "InfoStoredTime"

    fun saveStoredTime(key: String, storedTime: Long) = pref.edit().putLong(key, storedTime).apply()

    fun loadStoredTime(key: String): Long = pref.getLong(key, 0)
}