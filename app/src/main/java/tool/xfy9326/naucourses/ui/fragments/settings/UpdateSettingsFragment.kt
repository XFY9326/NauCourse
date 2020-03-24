package tool.xfy9326.naucourses.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.fragments.base.BaseSettingsPreferenceFragment

@Suppress("unused")
class UpdateSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_update

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(Constants.Pref.CheckUpdatesNow)?.setOnPreferenceClickListener {
            checkUpdates()
            true
        }
    }

    private fun checkUpdates() {

    }
}