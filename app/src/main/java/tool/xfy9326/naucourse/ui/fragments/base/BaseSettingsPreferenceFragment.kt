package tool.xfy9326.naucourse.ui.fragments.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.preference.PreferenceFragmentCompat
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.ui.activities.SettingsActivity.Companion.requireSettingsActivity

abstract class BaseSettingsPreferenceFragment : PreferenceFragmentCompat() {
    protected abstract val preferenceResId: Int

    @StringRes
    protected open val titleName: Int? = null

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsPref.prefName
        setPreferencesFromResource(preferenceResId, rootKey)
        onPrefViewInit(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        titleName?.let {
            requireSettingsActivity().toolBar.setTitle(it)
        }
    }

    protected open fun onPrefViewInit(savedInstanceState: Bundle?) {}
}