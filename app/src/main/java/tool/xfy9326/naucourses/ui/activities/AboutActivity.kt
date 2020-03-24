package tool.xfy9326.naucourses.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourses.BuildConfig
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourses.utils.views.DialogUtils

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(tb_general)
        enableHomeButton()
        setView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setView() {
        tv_aboutVersion.text = if (BuildConfig.DEBUG) {
            getString(
                R.string.version_detail,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            ) + Constants.SPACE + getString(R.string.debug_version)
        } else {
            getString(R.string.version_detail, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        }
        layout_aboutEULA.setOnClickListener {
            DialogUtils.createUsingLicenseDialog(this, lifecycle).show()
        }
        layout_aboutOpenSourceLicense.setOnClickListener {
            DialogUtils.createOpenSourceLicenseDialog(this, lifecycle).show()
        }
        layout_aboutDonate.setOnClickListener {

        }
    }
}