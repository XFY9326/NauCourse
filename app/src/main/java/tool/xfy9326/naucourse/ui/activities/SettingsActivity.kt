package tool.xfy9326.naucourse.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.fragments.settings.MainSettingsScreenFragment
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(tb_general)
        enableHomeButton()
        setupBaseSettingsFragment(savedInstanceState)
    }

    private fun setupBaseSettingsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fg_settingsContent, MainSettingsScreenFragment()).commit()
        }
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

    override fun onDestroy() {
        System.gc()
        super.onDestroy()
    }
}