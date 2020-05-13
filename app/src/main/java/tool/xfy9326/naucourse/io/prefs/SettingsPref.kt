package tool.xfy9326.naucourse.io.prefs

import android.graphics.Color
import android.widget.ImageView
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.io.prefs.base.BasePref

object SettingsPref : BasePref() {
    override val prefName: String = "Settings"

    // GeneralSettings
    val ExitApplicationDirectly by pref.boolean(defValue = false)
    val NotifyNextCourse by pref.boolean(defValue = true)


    // DisplaySettings
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
    val ExpandCourseDetailInDefault by pref.boolean(defValue = false)


    // DataSettings
    val AutoAsyncCourseData by pref.boolean(defValue = true)
    val AutoAsyncNewsInfo by pref.boolean(defValue = false)
    val AutoUpdateCourseArrange by pref.boolean(defValue = true)


    // CourseTableSettings
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
    val HighLightCourseTableTodayDate by pref.boolean(defValue = true)
    val CourseCellTextSize by pref.int(defValue = 3) // +10 表示SP单位的文字大小
    private val CourseTableBackgroundScareType by pref.string(defValue = ImageView.ScaleType.CENTER_CROP.name)
    fun getCourseTableBackgroundScareType() = ImageView.ScaleType.valueOf(CourseTableBackgroundScareType!!)

    enum class NotThisWeekCourseCellStyle {
        TEXT,
        COLOR
    }

    private val NotThisWeekCourseShowType by pref.stringSet(defValue = setOf(NotThisWeekCourseCellStyle.TEXT.name))

    fun getNotThisWeekCourseShowType(): HashSet<NotThisWeekCourseCellStyle> {
        val data = NotThisWeekCourseShowType!!
        val result = HashSet<NotThisWeekCourseCellStyle>(data.size)
        for (type in data) {
            result.add(NotThisWeekCourseCellStyle.valueOf(type))
        }
        return result
    }


    // UpdateSettings
    val AutoCheckUpdates by pref.boolean(defValue = true)
    val UseBrowserDownloadDirectLinkUpdate by pref.boolean(defValue = false)


    // DebugSettings
    val DebugMode by pref.boolean(defValue = BuildConfig.DEBUG)
    val DebugLogCatch by pref.boolean(defValue = false)
    val DebugExceptionCatch by pref.boolean(defValue = true)
    val CrashCatch by pref.boolean(defValue = true)
}