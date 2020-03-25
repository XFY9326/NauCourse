package tool.xfy9326.naucourse.ui.fragments.base

import android.os.Bundle
import android.view.Gravity
import androidx.annotation.CallSuper
import androidx.preference.PreferenceFragmentCompat
import androidx.transition.Slide
import tool.xfy9326.naucourse.io.prefs.SettingsPref

abstract class BaseSettingsPreferenceFragment : PreferenceFragmentCompat() {
    protected abstract val preferenceResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Slide(Gravity.END)
        exitTransition = Slide(Gravity.START)
    }

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsPref.prefName
        setPreferencesFromResource(preferenceResId, rootKey)
        onPrefViewInit(savedInstanceState)
    }

    protected open fun onPrefViewInit(savedInstanceState: Bundle?) {}
}