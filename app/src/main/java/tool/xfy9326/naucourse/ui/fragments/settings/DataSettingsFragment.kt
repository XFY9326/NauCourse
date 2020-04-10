package tool.xfy9326.naucourse.ui.fragments.settings

import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment

@Suppress("unused")
class DataSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_data
    override val titleName: Int = R.string.settings_data
}