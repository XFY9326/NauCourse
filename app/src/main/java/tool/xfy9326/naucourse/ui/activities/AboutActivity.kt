package tool.xfy9326.naucourse.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import tool.xfy9326.naucourse.utils.views.DialogUtils

class AboutActivity : AppCompatActivity() {
    private var advancedFunctionClickTime = 0

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
        @Suppress("ConstantConditionIf")
        tv_aboutVersion.text =
            if (BuildConfig.DEBUG) {
                getString(R.string.version_detail_debug, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
            } else {
                getString(R.string.version_detail, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
            }
        layout_aboutEULA.setOnClickListener {
            DialogUtils.createUsingLicenseDialog(this, lifecycle).show()
        }
        layout_aboutOpenSourceLicense.setOnClickListener {
            DialogUtils.createOpenSourceLicenseDialog(this, lifecycle).show()
        }
        layout_aboutApp.setOnClickListener {
            activeAdvancedFunction()
        }
    }

    private fun activeAdvancedFunction() {
        if (AppPref.EnableAdvancedFunctions) {
            showToast(this, R.string.advanced_function_on)
        } else {
            advancedFunctionClickTime++
            if (advancedFunctionClickTime >= Constants.Others.ADVANCED_FUNCTION_CLICK_TIME) {
                AppPref.EnableAdvancedFunctions = true
                NotifyBus[NotifyBus.Type.ADVANCED_FUNCTION_MODE_CHANGED].notifyEvent()
                showToast(this, R.string.advanced_function_on)
            }
        }
    }
}