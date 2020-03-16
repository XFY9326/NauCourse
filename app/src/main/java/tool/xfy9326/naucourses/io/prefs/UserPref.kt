package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.io.prefs.base.BasePref

object UserPref : BasePref() {
    override val prefName: String = "User"

    private const val USER_PASSWORD = "UserPassword"

    var UserId by pref.string(encrypted = true, commit = true)

    var UserPassword by pref.string(USER_PASSWORD, encrypted = true, commit = true)

    var HasLogin by pref.boolean(defValue = false, commit = true)
}