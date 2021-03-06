package tool.xfy9326.naucourse.io.prefs

import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.constants.CourseConst
import tool.xfy9326.naucourse.io.prefs.base.BasePref
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import java.util.*

object AppPref : BasePref() {
    override val prefName: String = "App"

    private const val CUSTOM_START_TERM_DATE_MILLS = "CustomStartTermDateMills"
    private const val CUSTOM_END_TERM_DATE_MILLS = "CustomEndTermDateMills"

    const val CURRENT_SCHOOL_CALENDAR_URL = "CurrentSchoolCalendarUrl"
    const val CURRENT_SCHOOL_CALENDAR_IMAGE_URL = "CurrentSchoolCalendarImageUrl"
    const val IGNORE_UPDATE_VERSION_CODE = "IgnoreUpdateVersionCode"
    const val FORCE_UPDATE_VERSION_CODE = "ForceUpdateVersionCode"

    private const val DEFAULT_TERM_DATE_MILLS = 0L
    private const val DEFAULT_LAST_CRASH_TIME_MILLS = 0L

    private var ShowNewsType by pref.stringSet()

    fun readShowNewsType(deleteUnknownSource: Boolean = true): Set<PostSource> {
        val savedResult = ShowNewsType
        return if (savedResult == null) {
            PostSource.values().filterNot {
                it == PostSource.UNKNOWN
            }.toSet()
        } else {
            GeneralNews.parseStringSet(savedResult, deleteUnknownSource)
        }
    }

    fun saveShowNewsType(set: Set<String>) {
        ShowNewsType = set
    }

    private var CustomStartTermDateMills by pref.long(CUSTOM_START_TERM_DATE_MILLS, DEFAULT_TERM_DATE_MILLS)

    private var CustomEndTermDateMills by pref.long(CUSTOM_END_TERM_DATE_MILLS, DEFAULT_TERM_DATE_MILLS)

    @Synchronized
    fun saveCustomTermDate(termDate: TermDate) {
        CustomStartTermDateMills = termDate.startDate.time
        CustomEndTermDateMills = termDate.endDate.time
    }

    @Synchronized
    fun clearCustomTermDate() {
        remove(CUSTOM_START_TERM_DATE_MILLS)
        remove(CUSTOM_END_TERM_DATE_MILLS)
    }

    @Synchronized
    fun readSavedCustomTermDate(): TermDate? {
        val startMills = CustomStartTermDateMills
        val endMills = CustomEndTermDateMills
        return if (startMills == DEFAULT_TERM_DATE_MILLS || endMills == DEFAULT_TERM_DATE_MILLS) {
            null
        } else {
            TermDate(Date(startMills), Date(endMills))
        }
    }

    var LastCrashTimeMills by pref.long(defValue = DEFAULT_LAST_CRASH_TIME_MILLS, commit = true)

    var LastInstalledVersionCode by pref.int(defValue = 0)

    var ShowArchiveAttention by pref.boolean(defValue = true)

    var CurrentSchoolCalendarUrl by pref.string(key = CURRENT_SCHOOL_CALENDAR_URL)
    var CurrentSchoolCalendarImageUrl by pref.string(key = CURRENT_SCHOOL_CALENDAR_IMAGE_URL)

    var MaxWeekNumCache by pref.int(defValue = CourseConst.MAX_WEEK_NUM_SIZE)

    var EnableAdvancedFunctions by pref.boolean(defValue = BuildConfig.DEBUG)

    var IgnoreUpdateVersionCode by pref.int(key = IGNORE_UPDATE_VERSION_CODE, defValue = 0)

    var ForceUpdateVersionCode by pref.int(key = FORCE_UPDATE_VERSION_CODE, defValue = 0)

    var UpdateDownloadId by pref.long(defValue = 0)

    var CourseTableBackgroundImageName by pref.string()

    var EditAsyncCourseAttention by pref.boolean(defValue = false)

    var LastNotifyCourseHash by pref.int(defValue = 0)
}