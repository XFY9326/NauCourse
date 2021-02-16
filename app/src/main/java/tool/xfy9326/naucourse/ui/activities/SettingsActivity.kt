package tool.xfy9326.naucourse.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.ActivitySettingsBinding
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.ui.activities.base.BaseActivity
import tool.xfy9326.naucourse.ui.fragments.settings.MainSettingsScreenFragment

class SettingsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    companion object {
        fun Fragment.requireSettingsActivity() = (requireActivity() as SettingsActivity)
    }

    private val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }
    val coordinatorLayout by lazy {
        binding.layoutSettings
    }
    val toolBar by lazy {
        binding.toolbar.tbGeneral
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.tbGeneral)
        enableHomeButton()
        setupBaseSettingsFragment(savedInstanceState)
    }

    private fun setupBaseSettingsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fg_settingsContent, MainSettingsScreenFragment()).commit()
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
        fragment.arguments = pref.extras

        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.fade_enter,
                R.anim.fade_exit,
                R.anim.fade_enter,
                R.anim.fade_exit
            )
            replace(R.id.fg_settingsContent, fragment)
            addToBackStack(null)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onRequestBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onRequestBack() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}