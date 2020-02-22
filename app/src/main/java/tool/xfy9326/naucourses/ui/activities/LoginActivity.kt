package tool.xfy9326.naucourses.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.view_login_panel.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourses.ui.models.activity.LoginViewModel
import tool.xfy9326.naucourses.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourses.utils.views.DialogUtils
import tool.xfy9326.naucourses.utils.views.I18NUtils
import tool.xfy9326.naucourses.utils.views.ViewUtils.clear


class LoginActivity : ViewModelActivity<LoginViewModel>() {

    override fun onCreateContentView() = R.layout.activity_login

    override fun onCreateViewModel() = ViewModelProvider(this)[LoginViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: LoginViewModel) {
        //TODO
        et_userId.setText("17013209")
        et_userPassword.setText("262010")

        tv_EULALicense.setOnClickListener {
            DialogUtils.createUsingLicenseDialog(this).show()
        }
        tv_forgetPassword.setOnClickListener {
            DialogUtils.createForgetPasswordDialog(this).show()
        }
        btn_login.setOnClickListener {
            val userId = et_userId.text.toString()
            val userPw = et_userPassword.text.toString()
            val acceptLicense = cb_acceptEULA.isChecked
            if (acceptLicense) {
                if (userId.isNotEmpty() && userId.isNotBlank() && userPw.isNotEmpty() && userPw.isNotBlank()) {
                    viewModel.doLogin(userId, userPw)
                } else {
                    showSnackBar(layout_activityLogin, R.string.login_info_empty)
                }
            } else {
                showSnackBar(layout_activityLogin, R.string.eula_not_accept)
            }
        }
    }

    override fun bindViewModel(viewModel: LoginViewModel) {
        viewModel.cachedUserId.observe(this, Observer {
            et_userId.setText(it)
        })
        viewModel.isLoginLoading.observe(this, Observer {
            tv_loadingMsg.clear()
            et_userId.isEnabled = !it
            et_userPassword.isEnabled = !it
            tv_EULALicense.isEnabled = !it
            tv_forgetPassword.isEnabled = !it
            cb_acceptEULA.isEnabled = !it
            btn_login.isEnabled = !it

            if (it) {
                btn_login.visibility = View.GONE
                btn_login.animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
                anim_loginLoading.visibility = View.VISIBLE
                anim_loginLoading.animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            } else {
                anim_loginLoading.visibility = View.GONE
                anim_loginLoading.animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
                btn_login.visibility = View.VISIBLE
                btn_login.animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            }
        })
        viewModel.errorReasonType.observe(this, Observer {
            showSnackBar(layout_activityLogin, I18NUtils.getErrorMsgResId(it)!!)
        })
        viewModel.loginSuccess.observe(this, Observer {
            if (it) {
                startActivity(Intent(this, MainDrawerActivity::class.java))
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                finishAfterTransition()
            }
        })
        viewModel.loginProcess.observe(this, Observer {
            val resId = I18NUtils.getLoadingProcessResId(it)
            if (resId != null) {
                tv_loadingMsg.setText(resId)
            } else {
                tv_loadingMsg.text = Constants.EMPTY
            }
        })
    }

    override fun onBackPressed() {
        moveTaskToBack(false)
    }
}