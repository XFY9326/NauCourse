package tool.xfy9326.naucourse.io.prefs

import android.graphics.Color
import android.widget.ImageView
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


    // DataSettingsFragment
    val AutoAsyncCourseData by pref.boolean(defValue = true)

    // CourseTableSettingsFragment
    val ShowNextWeekCourseTableAhead by pref.boolean(defValue = true)
    val CourseTableRoundCompat by pref.boolean(defValue = false)
    val ForceShowCourseTableWeekends by pref.boolean(defValue = false)
    val SameCourseCellHeight by pref.boolean(defValue = true)
    val CenterHorizontalShowCourseText by pref.boolean(defValue = false)
    val CenterVerticalShowCourseText by pref.boolean(defValue = false)
    val DrawAllCellBackground by pref.boolean(defValue = false)
    val UseRoundCornerCourseCell by pref.boolean(defValue = true)
    val CustomCourseTableBackground by pref.boolean(defValue = false)
    val CustomCourseTableAlpha by pref.int(defValue = 100)
    val CourseTableImageQuality by pref.int(defValue = 60)
    val CourseTableBackgroundAlpha by pref.int(defValue = 100)
    val CourseTableBackgroundFullScreen by pref.boolean(defValue = true)
    val ShowNotThisWeekCourseInTable by pref.boolean(defValue = true)
    val EnableCourseTableTimeTextColor by pref.boolean(defValue = false)
    val CourseTableTimeTextColor by pref.int(defValue = Color.BLACK)
    private val CourseTableBackgroundScareType by pref.string(defValue = ImageView.ScaleType.CENTER_CROP.name)

    fun getCourseTableBackgroundScareType() = ImageView.ScaleType.valueOf(CourseTableBackgroundScareType!!)


    // UpdateSettingsFragment
    val AutoCheckUpdates by pref.boolean(defValue = true)
    val AutoAsyncNewsInfo by pref.boolean(defValue = true)

    // DebugSettingsFragment
    val DebugMode by pref.boolean(defValue = BuildConfig.DEBUG)
    val DebugLogCatch by pref.boolean(defValue = false)
    val DebugExceptionCatch by pref.boolean(defValue = true)
    val CrashCatch by pref.boolean(defValue = true)
}