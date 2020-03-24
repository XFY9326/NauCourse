package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.io.prefs.base.BasePref
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import java.util.*

object AppPref : BasePref() {
    private const val CUSTOM_START_TERM_DATE_MILLS = "CustomStartTermDateMills"
    private const val CUSTOM_END_TERM_DATE_MILLS = "CustomEndTermDateMills"
    private const val DEFAULT_TERM_DATE_MILLS = 0L
    private const val DEFAULT_LAST_CRASH_TIME_MILLS = 0L

    private var ShowNewsType by pref.stringSet()

    fun readShowNewsType(deleteUnknownSource: Boolean = true): Set<GeneralNews.PostSource> {
        val savedResult = ShowNewsType
        return if (savedResult == null) {
            GeneralNews.PostSource.values().filterNot {
                it == GeneralNews.PostSource.UNKNOWN
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
}