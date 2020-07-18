package tool.xfy9326.naucourse.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.PrefConst
import tool.xfy9326.naucourse.ui.activities.AboutActivity
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.utils.BaseUtils

class MainSettingsScreenFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_main_screen
    override val titleName: Int = R.string.settings

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(PrefConst.AboutIntent)?.setOnPreferenceClickListener {
            startActivity(Intent(requireActivity(), AboutActivity::class.java))
            false
        }
        if (!BuildConfig.DEBUG && BaseUtils.isBeta()) {
            findPreference<Preference>(PrefConst.ApplicationUpdate)?.isVisible = false
        }
    }
}