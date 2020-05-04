package tool.xfy9326.naucourse.io.prefs

import tool.xfy9326.naucourse.io.prefs.base.BasePref
import java.util.*

object DevicePref : BasePref() {
    override val prefName: String = "Device"

    val deviceId by pref.string(defValue = UUID.randomUUID().toString())
}