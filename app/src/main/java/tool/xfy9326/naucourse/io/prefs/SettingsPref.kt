package tool.xfy9326.naucourse.io.prefs

import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.io.prefs.base.BasePref

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

    enum class EnterInterfaceType {
        COURSE_ARRANGE,
        COURSE_TABLE,
        NEWS
    }

    private val DefaultEnterInterface by pref.string(defValue = EnterInterfaceType.COURSE_ARRANGE.name)

    fun getDefaultEnterInterface() = EnterInterfaceType.valueOf(DefaultEnterInterface!!)

    val UseBrowserOpenNewsDetail by pref.boolean(defValue = false)


    // CourseTableSettingsFragment
    val CourseTableRoundCompat by pref.boolean(defValue = false)
    val ForceShowCourseTableWeekends by pref.boolean(defValue = false)
    val SameCourseCellHeight by pref.boolean(defValue = true)
    val CenterHorizontalShowCourseText by pref.boolean(defValue = false)

    // UpdateSettingsFragment
    val AutoCheckUpdates by pref.boolean(defValue = true)

    // DebugSettingsFragment
    val DebugMode by pref.boolean(defValue = BuildConfig.DEBUG)
    val DebugLogCatch by pref.boolean(defValue = false)
    val DebugExceptionCatch by pref.boolean(defValue = true)
    val CrashCatch by pref.boolean(defValue = true)
}