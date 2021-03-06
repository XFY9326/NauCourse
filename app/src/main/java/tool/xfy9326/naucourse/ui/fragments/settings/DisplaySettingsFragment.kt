package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.PrefConst
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.utils.BaseUtils

@Suppress("unused")
class DisplaySettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_display
    override val titleName: Int = R.string.display

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<ListPreference>(PrefConst.NightMode)?.setOnPreferenceChangeListener { _, newValue ->
            if (SettingsPref.NightMode != newValue) {
                changeNightModeTheme(SettingsPref.NightModeType.valueOf(newValue as String))
            }
            true
        }
        findPreference<ListPreference>(PrefConst.DefaultEnterInterface)?.setOnPreferenceChangeListener { _, newValue ->
            if (SettingsPref.getDefaultEnterInterface().name != newValue) {
                NotifyBus[NotifyType.DEFAULT_ENTER_INTERFACE_CHANGED].notifyEvent()
            }
            true
        }
    }

    @Synchronized
    private fun changeNightModeTheme(type: SettingsPref.NightModeType) {
        val newMode = BaseUtils.getNightModeInt(type)
        if (AppCompatDelegate.getDefaultNightMode() != newMode) {
            NotifyBus[NotifyType.NIGHT_MODE_CHANGED].notifyEvent()

            requireActivity().window.setWindowAnimations(R.style.AppTheme_NightModeTransitionAnimation)
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
    }
}