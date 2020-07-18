package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.view_login_panel.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.dialogs.UpdateDialog
import tool.xfy9326.naucourse.ui.models.activity.LoginViewModel
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourse.utils.views.AnimUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils.clear


class LoginActivity : ViewModelActivity<LoginViewModel>() {
    private lateinit var loadingAnimateDrawable: AnimatedVectorDrawableCompat

    override fun onCreateContentView() = R.layout.activity_login

    override fun onCreateViewModel() = ViewModelProvider(this)[LoginViewModel::class.java]

    override fun onStart() {
        super.onStart()
        getViewModel().checkUpdate()
        AppWidgetUtils.clearWidget()
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: LoginViewModel) {
        loadingAnimateDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_anim_loading)!!
        iv_loginLoading.setImageDrawable(loadingAnimateDrawable)
        viewModel.requestSavedCacheUserId()

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
        viewModel.savedCacheUserId.observeEvent(this, Observer {
            et_userId.setText(it)
        })
        viewModel.errorReasonType.observeEvent(this, Observer {
            showSnackBar(layout_activityLogin, I18NUtils.getErrorMsgResId(it)!!)
        })
        viewModel.loginSuccess.observeNotification(this, {
            startActivity(Intent(this, MainDrawerActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finishAfterTransition()
        })
        viewModel.loginProcess.observeEvent(this, Observer {
            val resId = I18NUtils.getLoadingProcessResId(it)
            if (resId != null) {
                tv_loadingMsg.setText(resId)
            } else {
                tv_loadingMsg.text = BaseConst.EMPTY
            }
        })
        viewModel.compatData.observeEvent(this, Observer {
            et_userId.setText(it.userId)
            et_userPassword.setText(it.userPw)
            cb_acceptEULA.isChecked = true
        })
        viewModel.updateInfo.observeEvent(this, Observer { data ->
            viewModel.isLoginLoading.value?.peekContent()?.let {
                if (!it) {
                    UpdateDialog.showDialog(supportFragmentManager, data)
                }
            }
        })
    }

    private fun setLoadingAnimation(setShow: Boolean) {
        if (setShow) {
            setupAnimationWidget(iv_loginLoading, btn_login)
            loadingAnimateDrawable.registerAnimationCallback(AnimUtils.getAnimationLoopCallback())
            ViewUtils.tryStartAnimateDrawable(loadingAnimateDrawable)
        } else {
            loadingAnimateDrawable.clearAnimationCallbacks()
            ViewUtils.tryStopAnimateDrawable(loadingAnimateDrawable)
            setupAnimationWidget(btn_login, iv_loginLoading)
        }
    }

    private fun setupAnimationWidget(visibleView: View, goneView: View) {
        goneView.apply {
            visibility = View.GONE
            animation = AnimUtils.getAnimationFadeGone(this@LoginActivity)
        }

        visibleView.apply {
            visibility = View.VISIBLE
            animation = AnimUtils.getAnimationFadeVisible(this@LoginActivity)
        }
    }

    override fun onDestroy() {
        loadingAnimateDrawable.clearAnimationCallbacks()
        super.onDestroy()
    }
}