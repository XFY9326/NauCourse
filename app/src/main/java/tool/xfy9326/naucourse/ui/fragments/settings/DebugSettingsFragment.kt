package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.PrefConst
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.ui.activities.SettingsActivity.Companion.requireSettingsActivity
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.utils.debug.DebugIOUtils

@Suppress("unused")
class DebugSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_debug
    override val titleName: Int = R.string.settings_debug

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<CheckBoxPreference>(PrefConst.DebugMode)?.isChecked = SettingsPref.DebugMode
        findPreference<Preference>(PrefConst.ClearDebugLogs)?.setOnPreferenceClickListener {
            if (DebugIOUtils.clearLogs()) {
                requireSettingsActivity().coordinatorLayout.showSnackBar(R.string.operation_success)
            } else {
                requireSettingsActivity().coordinatorLayout.showSnackBar(R.string.operation_failed)
            }
            false
        }
    }
}