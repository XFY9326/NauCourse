package tool.xfy9326.naucourses.ui.fragments.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.io.prefs.SettingsPref
import tool.xfy9326.naucourses.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourses.utils.BaseUtils

@Suppress("unused")
class DisplaySettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_display

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<ListPreference>(Constants.Pref.NightMode)?.setOnPreferenceChangeListener { _, newValue ->
            if (SettingsPref.NightMode != newValue) {
                changeNightModeTheme(SettingsPref.NightModeType.valueOf(newValue as String))
            }
            true
        }
    }

    @Synchronized
    private fun changeNightModeTheme(type: SettingsPref.NightModeType) {
        val newMode = BaseUtils.getNightModeInt(type)
        if (AppCompatDelegate.getDefaultNightMode() != newMode) {
            requireActivity().window.setWindowAnimations(R.style.AppTheme_NightModeTransitionAnimation)
            AppCompatDelegate.setDefaultNightMode(newMode)
            App.instance.nightModeChanged.notifyEvent()
        }
    }
}