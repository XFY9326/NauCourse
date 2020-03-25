package tool.xfy9326.naucourse.ui.fragments.settings

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment

@Suppress("unused")
class CourseTableSettingsFragment : BaseSettingsPreferenceFragment(), Preference.OnPreferenceChangeListener {

    override val preferenceResId = R.xml.settings_course_table

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<CheckBoxPreference>(Constants.Pref.ForceShowCourseTableWeekends)?.onPreferenceChangeListener = this
        findPreference<CheckBoxPreference>(Constants.Pref.SameCourseCellHeight)?.onPreferenceChangeListener = this
        findPreference<CheckBoxPreference>(Constants.Pref.CenterHorizontalShowCourseText)?.onPreferenceChangeListener = this
        findPreference<CheckBoxPreference>(Constants.Pref.CourseTableRoundCompat)?.onPreferenceChangeListener = this
    }

    // 刷新课表视图
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        App.instance.requestRebuildCourseTable.notifyEvent()
        return true
    }
}