package tool.xfy9326.naucourse.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.compat.OldDataCompat
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils


class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.getBooleanExtra(BaseUtils.CRASH_RESTART_FLAG, false) == true) {
            Toast.makeText(App.instance, R.string.crash_msg, Toast.LENGTH_SHORT).show()
        }
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
        } else {
            selectStartActivity()
        }
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