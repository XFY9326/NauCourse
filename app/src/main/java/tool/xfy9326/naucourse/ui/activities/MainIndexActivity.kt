package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.compat.OldDataCompat
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.ui.activities.base.BaseActivity
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils


class MainIndexActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0 -> finish()
            intent?.getBooleanExtra(BaseUtils.SHOW_ERROR_ACTIVITY_FLAG, false) == true -> startAppErrorActivity()
            else -> {
                if (intent?.getBooleanExtra(BaseUtils.CRASH_RESTART_FLAG, false) == true) {
                    App.instance.showShortToast(R.string.crash_msg)
                }
                selectStartActivity()
            }
        }
    }

    private fun startAppErrorActivity() {
        startActivity(Intent(this, ErrorActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finishAfterTransition()
    }

    private fun selectStartActivity() {
        val isNewVersion = versionRecord()
        val intent = if (AccountUtils.validateUserLoginStatus()) {
            Intent(this, MainDrawerActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java).apply {
                if (isNewVersion) putExtra(IntentUtils.UPDATE_FROM_OLD_DATA_FLAG, OldDataCompat.hasOldData())
            }
        }
        startActivity(intent.putExtra(IntentUtils.NEW_VERSION_FLAG, isNewVersion))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finishAfterTransition()
    }

    private fun versionRecord() =
        (BuildConfig.VERSION_CODE != AppPref.LastInstalledVersionCode).also {
            if (it) AppPref.LastInstalledVersionCode = BuildConfig.VERSION_CODE
        }

    override fun onBackPressed() {}
}