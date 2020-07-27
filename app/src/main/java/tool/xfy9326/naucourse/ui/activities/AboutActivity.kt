package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.kt.bindLifecycle
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
import tool.xfy9326.naucourse.ui.activities.base.BaseActivity
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils

class AboutActivity : BaseActivity() {
    private var advancedFunctionClickTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(tb_general)
        enableHomeButton()
        setView()
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
        layout_aboutFeedbackOnline.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }
        layout_aboutFeedbackQQGroup.setOnClickListener {
            IntentUtils.joinFeedbackQQGroup(this)
        }
    }

    private fun activeAdvancedFunction() {
        if (AppPref.EnableAdvancedFunctions) {
            showShortToast(R.string.advanced_function_on)
        } else {
            advancedFunctionClickTime++
            if (advancedFunctionClickTime >= OthersConst.ADVANCED_FUNCTION_CLICK_TIME) {
                showAdvancedFunctionDialog()
                advancedFunctionClickTime = 0
            }
        }
    }

    private fun showAdvancedFunctionDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.attention)
            setMessage(R.string.advanced_function_warning)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                AppPref.EnableAdvancedFunctions = true
                NotifyBus[NotifyType.ADVANCED_FUNCTION_MODE_CHANGED].notifyEvent()
                showShortToast(R.string.advanced_function_on)
            }
            setCancelable(false)
        }.create().also {
            it.bindLifecycle(lifecycle)
        }.show()
    }
}