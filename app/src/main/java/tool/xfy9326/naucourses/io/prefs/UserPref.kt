package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.io.prefs.base.BasePref

object UserPref : BasePref() {
    override val prefName: String = "User"

    const val USER_PASSWORD = "UserPassword"
    const val CARD_BALANCE = "CardBalance"

    var UserId by pref.string()

    var UserPassword by pref.string(USER_PASSWORD)

    var UUID by pref.string()

    var HasLogin by pref.boolean(defValue = false)

    var CardBalance by pref.float(CARD_BALANCE, defValue = -1f)
}