package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.BuildConfig
import tool.xfy9326.naucourses.io.prefs.base.BasePref

object SettingsPref : BasePref() {
    override val prefName: String = "Settings"

    // DisplaySettingsFragment
    enum class NightModeType {
        AUTO,
        ENABLED,
        DISABLED
    }

    val NightMode by pref.string(defValue = NightModeType.AUTO.name)

    fun getNightMode() = NightModeType.valueOf(NightMode!!)


    // CourseTableSettingsFragment
    var CourseTableRoundCompat by pref.boolean(defValue = false)


    // DebugSettingsFragment
    var DebugMode by pref.boolean(defValue = BuildConfig.DEBUG)
    var DebugLogCatch by pref.boolean(defValue = false)
    var DebugExceptionCatch by pref.boolean(defValue = true)
    var CrashCatch by pref.boolean(defValue = true)
}