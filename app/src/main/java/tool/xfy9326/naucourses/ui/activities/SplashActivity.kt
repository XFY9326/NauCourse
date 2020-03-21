package tool.xfy9326.naucourses.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.utils.BaseUtils
import tool.xfy9326.naucourses.utils.secure.AccountUtils


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
        if (AccountUtils.validateUserLoginStatus()) {
            startActivity(Intent(this, MainDrawerActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finishAfterTransition()
    }

    override fun onBackPressed() {}
}