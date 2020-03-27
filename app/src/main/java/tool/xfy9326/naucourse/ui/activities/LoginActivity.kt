package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.view_login_panel.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.LoginViewModel
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourse.utils.views.AnimUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils.clear


class LoginActivity : ViewModelActivity<LoginViewModel>() {
    override fun onCreateContentView() = R.layout.activity_login

    override fun onCreateViewModel() = ViewModelProvider(this)[LoginViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: LoginViewModel) {
        AccountUtils.readSavedCacheUserId()?.let {
            et_userId.setText(it)
        }
        tv_EULALicense.setOnClickListener {
            DialogUtils.createUsingLicenseDialog(this, lifecycle).show()
        }
        tv_forgetPassword.setOnClickListener {
            DialogUtils.createForgetPasswordDialog(this, lifecycle).show()
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
        intent?.getBooleanExtra(IntentUtils.UPDATE_FROM_OLD_DATA_FLAG, false)?.let {
            if (it) viewModel.doLoginFromOldData()
        }
    }

    override fun bindViewModel(viewModel: LoginViewModel) {
        viewModel.isLoginLoading.observeEvent(this, Observer {
            tv_loadingMsg.clear()
            et_userId.isEnabled = !it
            et_userPassword.isEnabled = !it
            tv_EULALicense.isEnabled = !it
            tv_forgetPassword.isEnabled = !it
            cb_acceptEULA.isEnabled = !it
            btn_login.isEnabled = !it

            setLoadingAnimation(it)
        })
        viewModel.errorReasonType.observeEvent(this, Observer {
            showSnackBar(layout_activityLogin, I18NUtils.getErrorMsgResId(it)!!)
        })
        viewModel.loginSuccess.observeNotification(this, {
            System.gc()
            startActivity(Intent(this, MainDrawerActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            finishAfterTransition()
        })
        viewModel.loginProcess.observeEvent(this, Observer {
            val resId = I18NUtils.getLoadingProcessResId(it)
            if (resId != null) {
                tv_loadingMsg.setText(resId)
            } else {
                tv_loadingMsg.text = Constants.EMPTY
            }
        })
        viewModel.compatData.observeEvent(this, Observer {
            et_userId.setText(it.userId)
            et_userPassword.setText(it.userPw)
            cb_acceptEULA.isChecked = true
        })
    }

    private fun setLoadingAnimation(setShow: Boolean) {
        (iv_loginLoading.drawable as AnimatedVectorDrawable).apply {
            if (setShow) {
                btn_login.apply {
                    visibility = View.GONE
                    animation = AnimUtils.getAnimationFadeGone(this@LoginActivity)
                }

                iv_loginLoading.apply {
                    visibility = View.VISIBLE
                    animation = AnimUtils.getAnimationFadeVisible(this@LoginActivity)
                }
                AnimatedVectorDrawableCompat.registerAnimationCallback(this, AnimUtils.getAnimationLoopCallback())
                start()
            } else {
                AnimatedVectorDrawableCompat.clearAnimationCallbacks(this)
                stop()
                iv_loginLoading.apply {
                    visibility = View.GONE
                    animation = AnimUtils.getAnimationFadeGone(this@LoginActivity)
                }

                btn_login.apply {
                    visibility = View.VISIBLE
                    animation = AnimUtils.getAnimationFadeVisible(this@LoginActivity)
                }
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(false)
    }

    override fun onDestroy() {
        AnimatedVectorDrawableCompat.clearAnimationCallbacks(iv_loginLoading.drawable)
        System.gc()
        super.onDestroy()
    }
}