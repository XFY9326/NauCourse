package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment

@Suppress("unused")
class DebugSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_debug

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<CheckBoxPreference>(Constants.Pref.DebugMode)?.isChecked = SettingsPref.DebugMode
    }
}