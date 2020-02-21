package tool.xfy9326.naucourses.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import tool.xfy9326.naucourses.utils.secure.AccountUtils

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            selectStartActivity()
        }
    }

    override fun onEnterAnimationComplete() {
        if (isTaskRoot) {
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