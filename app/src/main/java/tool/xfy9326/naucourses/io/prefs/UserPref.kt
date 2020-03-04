package tool.xfy9326.naucourses.io.prefs

import tool.xfy9326.naucourses.io.prefs.base.BasePref

object UserPref : BasePref() {
    override val prefName: String = "User"

    private const val USER_PASSWORD = "UserPassword"
    const val CARD_BALANCE = "CardBalance_"
    const val CARD_BALANCE_DEFAULT_VALUE = -1f

    var UserId by pref.string(encrypted = true, commit = true)

    var UserPassword by pref.string(USER_PASSWORD, encrypted = true, commit = true)

    var HasLogin by pref.boolean(defValue = false, commit = true)

    // 解决Float类型无法被加密的问题
    private var CardBalance_ by pref.string(CARD_BALANCE, encrypted = true)

    var CardBalance: Float
        get() {
            val readValue = CardBalance_
            return if (readValue == null) {
                CARD_BALANCE_DEFAULT_VALUE
            } else {
                readValue.toFloatOrNull() ?: CARD_BALANCE_DEFAULT_VALUE
            }
        }
        set(value) {
            CardBalance_ = value.toString()
        }
}