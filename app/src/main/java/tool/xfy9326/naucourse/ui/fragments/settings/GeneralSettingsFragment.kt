package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.PrefConst
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.utils.utility.IntentUtils

@Suppress("unused")
class GeneralSettingsFragment : BaseSettingsPreferenceFragment() {
    override val preferenceResId: Int = R.xml.settings_general
    override val titleName: Int = R.string.general

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<CheckBoxPreference>(PrefConst.NotifyNextCourse)?.setOnPreferenceChangeListener { _, _ ->
            IntentUtils.startNextCourseAlarm(requireContext())
            true
        }
    }
}