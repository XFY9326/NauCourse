package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment

@Suppress("unused")
class UpdateSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_update
    override val titleName: Int = R.string.settings_update

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(Constants.Pref.CheckUpdatesNow)?.setOnPreferenceClickListener {
            checkUpdates()
            true
        }
    }

    private fun checkUpdates() {

    }
}