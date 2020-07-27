package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_app_error.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.ui.dialogs.UpdateDialog
import tool.xfy9326.naucourse.update.UpdateChecker
import tool.xfy9326.naucourse.utils.utility.IntentUtils

class AppErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_error)
        setSupportActionBar(tb_general)
        initView()
        checkUpdates(false)
    }

    private fun initView() {
        btn_errUpdateCheck.setOnClickListener {
            checkUpdates(true)
        }
        btn_errAddQQGroup.setOnClickListener {
            IntentUtils.joinFeedbackQQGroup(this)
        }
        btn_errFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }
    }

    private fun checkUpdates(showToast: Boolean) {
        if (showToast) showShortToast(R.string.checking_update)
        lifecycleScope.launch {
            val updateInfo = UpdateChecker.getNewUpdateInfo(true)
            if (updateInfo == null) {
                if (showToast) showShortToast(R.string.update_check_failed)
            } else {
                if (updateInfo.first) {
                    UpdateDialog.showDialog(supportFragmentManager, updateInfo.second!!)
                } else {
                    if (showToast) showShortToast(R.string.no_new_update)
                }
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}