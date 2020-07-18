package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import kotlinx.android.synthetic.main.activity_settings.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.PrefConst
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar

@Suppress("unused")
class DataSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_data
    override val titleName: Int = R.string.settings_data

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(PrefConst.ClearNetworkCache)?.setOnPreferenceClickListener {
            if (BaseUtils.clearCache(requireContext())) {
                showSnackBar(requireActivity().layout_settings, R.string.operation_success)
            } else {
                showSnackBar(requireActivity().layout_settings, R.string.operation_failed)
            }
            false
        }
    }
}