package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.io.prefs.base.BasePref

object CourseTablePref : BasePref() {
    override val prefName: String = "CourseTable"

    var RoundCornerDeviceCompat by pref.boolean(defValue = false)
}