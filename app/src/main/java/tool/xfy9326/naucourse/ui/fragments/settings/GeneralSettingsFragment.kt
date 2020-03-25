package tool.xfy9326.naucourse.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.activities.AboutActivity
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment

class GeneralSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_general

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(Constants.Pref.AboutIntent)?.setOnPreferenceClickListener {
            startActivity(Intent(requireActivity(), AboutActivity::class.java))
            false
        }
    }
}