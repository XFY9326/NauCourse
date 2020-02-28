package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.io.prefs.base.BasePref
import tool.xfy9326.naucourses.providers.beans.GeneralNews

object AppPref : BasePref() {
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
}