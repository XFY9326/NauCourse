package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.ActivityErrorBinding
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.ui.dialogs.UpdateDialog
import tool.xfy9326.naucourse.update.UpdateChecker
import tool.xfy9326.naucourse.utils.utility.IntentUtils

class ErrorActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IS_LOGIN_FAILED_ERROR = "IS_LOGIN_FAILED_ERROR"
    }

    private val binding by lazy {
        ActivityErrorBinding.inflate(layoutInflater)
    }

    private var isLoginError: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLoginError = intent.getBooleanExtra(EXTRA_IS_LOGIN_FAILED_ERROR, false)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.tbGeneral)
        initView()
        checkUpdates(false)
    }

    private fun initView() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        if (isLoginError) {
            supportActionBar?.setTitle(R.string.login_failed_help)
            binding.tvErrorHelp.setText(R.string.login_failed_attention)
        } else {
            supportActionBar?.setTitle(R.string.app_error)
            binding.tvErrorHelp.setText(R.string.app_error_attention)
        }

        binding.btnErrUpdateCheck.setOnClickListener {
            checkUpdates(true)
        }
        binding.btnErrAddQQGroup.setOnClickListener {
            IntentUtils.joinFeedbackQQGroup(this)
        }
        binding.btnErrFeedback.setOnClickListener {
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
        if (isLoginError) {
            super.onBackPressed()
        } else {
            finishAffinity()
        }
    }
}