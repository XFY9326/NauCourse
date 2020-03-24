package tool.xfy9326.naucourses.ui.fragments.settings

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.io.prefs.SettingsPref
import tool.xfy9326.naucourses.ui.fragments.base.BaseSettingsPreferenceFragment

@Suppress("unused")
class DebugSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_debug

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<CheckBoxPreference>(Constants.Pref.DebugMode)?.isChecked = SettingsPref.DebugMode
    }
}