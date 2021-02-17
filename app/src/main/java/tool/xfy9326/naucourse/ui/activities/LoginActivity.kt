package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.databinding.ActivityLoginBinding
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.kt.clear
import tool.xfy9326.naucourse.kt.showLongToast
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.kt.showSnackBarWithCallback
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.dialogs.UpdateDialog
import tool.xfy9326.naucourse.ui.models.activity.LoginViewModel
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.AnimUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils


class LoginActivity : ViewModelActivity<LoginViewModel>() {
    companion object {
        const val INTENT_PASSWORD_ERROR_LOGIN = "PASSWORD_ERROR_LOGIN"
    }

    private var passwordErrorLogin = false
    private lateinit var loadingAnimateDrawable: AnimatedVectorDrawableCompat

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreateContentView() = binding.root

    override fun onCreateViewModel() = ViewModelProvider(this)[LoginViewModel::class.java]

    override fun onStart() {
        super.onStart()
        if (!passwordErrorLogin) {
            getViewModel().checkUpdate()
            AppWidgetUtils.clearWidget()
        }
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: LoginViewModel) {
        passwordErrorLogin = intent.getBooleanExtra(INTENT_PASSWORD_ERROR_LOGIN, false)
        if (passwordErrorLogin) {
            binding.panel.etUserId.inputType = InputType.TYPE_NULL
            binding.panel.etUserId.isClickable = false
            binding.panel.cbAcceptEULA.isChecked = true
            showLongToast(R.string.password_change_error)
        }

        loadingAnimateDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_anim_loading)!!
        binding.panel.ivLoginLoading.setImageDrawable(loadingAnimateDrawable)
        viewModel.requestSavedCacheUserId()

        binding.panel.tvEULALicense.setOnClickListener {
            DialogUtils.createUsingLicenseDialog(this, lifecycle).show()
        }
        binding.panel.tvForgetPassword.setOnClickListener {
            DialogUtils.createForgetPasswordDialog(this, lifecycle).show()
        }
        binding.panel.btnLogin.setOnClickListener {
            val userId = binding.panel.etUserId.text.toString()
            val userPw = binding.panel.etUserPassword.text.toString()
            val acceptLicense = binding.panel.cbAcceptEULA.isChecked
            if (acceptLicense) {
                if (userId.isNotEmpty() && userId.isNotBlank() && userPw.isNotEmpty() && userPw.isNotBlank()) {
                    if (passwordErrorLogin) {
                        viewModel.changePasswordLogin(userId, userPw)
                    } else {
                        viewModel.doLogin(userId, userPw)
                    }
                } else {
                    binding.layoutActivityLogin.showSnackBar(R.string.login_info_empty)
                }
            } else {
                binding.layoutActivityLogin.showSnackBar(R.string.eula_not_accept)
            }
        }
        intent.getBooleanExtra(IntentUtils.UPDATE_FROM_OLD_DATA_FLAG, false).let {
            if (it) viewModel.doLoginFromOldData()
        }
        if (AppPref.ShowArchiveAttention) {
            DialogUtils.createArchiveAttentionDialog(this, lifecycle).show()
        }
    }

    override fun bindViewModel(viewModel: LoginViewModel) {
        viewModel.isLoginLoading.observeEvent(this) {
            binding.panel.tvLoadingMsg.clear()
            binding.panel.etUserId.isEnabled = !it
            binding.panel.etUserPassword.isEnabled = !it
            binding.panel.tvEULALicense.isEnabled = !it
            binding.panel.tvForgetPassword.isEnabled = !it
            binding.panel.cbAcceptEULA.isEnabled = !it
            binding.panel.btnLogin.isEnabled = !it

            setLoadingAnimation(it)
        }
        viewModel.savedCacheUserId.observeEvent(this) {
            binding.panel.etUserId.setText(it)
        }
        viewModel.errorReasonType.observeEvent(this) {
            binding.layoutActivityLogin.showSnackBarWithCallback(I18NUtils.getErrorMsgResId(it)!!, R.string.login_troubleshoot) {
                startActivity(Intent(this, ErrorActivity::class.java).apply {
                    putExtra(ErrorActivity.EXTRA_IS_LOGIN_FAILED_ERROR, true)
                })
            }
        }
        viewModel.loginSuccess.observeNotification(this) {
            startActivity(Intent(this, MainDrawerActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finishAfterTransition()
        }
        viewModel.loginProcess.observeEvent(this) {
            val resId = I18NUtils.getLoadingProcessResId(it)
            if (resId != null) {
                binding.panel.tvLoadingMsg.setText(resId)
            } else {
                binding.panel.tvLoadingMsg.text = BaseConst.EMPTY
            }
        }
        viewModel.compatData.observeEvent(this) {
            binding.panel.etUserId.setText(it.userId)
            binding.panel.etUserPassword.setText(it.userPw)
            binding.panel.cbAcceptEULA.isChecked = true
        }
        viewModel.updateInfo.observeEvent(this) { data ->
            viewModel.isLoginLoading.value?.let {
                if (!it) {
                    UpdateDialog.showDialog(supportFragmentManager, data)
                }
            }
        }
    }

    private fun setLoadingAnimation(setShow: Boolean) {
        if (setShow) {
            setupAnimationWidget(binding.panel.ivLoginLoading, binding.panel.btnLogin)
            loadingAnimateDrawable.registerAnimationCallback(AnimUtils.getAnimationLoopCallback())
            ViewUtils.tryStartAnimateDrawable(loadingAnimateDrawable)
        } else {
            loadingAnimateDrawable.clearAnimationCallbacks()
            ViewUtils.tryStopAnimateDrawable(loadingAnimateDrawable)
            setupAnimationWidget(binding.panel.btnLogin, binding.panel.ivLoginLoading)
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