package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.PrefConst
import tool.xfy9326.naucourse.ui.dialogs.UpdateDialog
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.update.UpdateChecker
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast

@Suppress("unused")
class UpdateSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId = R.xml.settings_update
    override val titleName: Int = R.string.settings_update

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(PrefConst.CheckUpdatesNow)?.setOnPreferenceClickListener {
            checkUpdates()
            true
        }
    }

    private fun checkUpdates() {
        showToast(R.string.checking_update)
        lifecycleScope.launch {
            val updateInfo = UpdateChecker.getNewUpdateInfo(true)
            if (updateInfo == null) {
                showToast(R.string.update_check_failed)
            } else {
                if (updateInfo.first) {
                    UpdateDialog.showDialog(parentFragmentManager, updateInfo.second!!)
                } else {
                    showToast(R.string.no_new_update)
                }
            }
        }
    }
}