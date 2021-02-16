package tool.xfy9326.naucourse.ui.activities.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.view.isVisible
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.ActivityWebviewBinding
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.utils.utility.IntentUtils

abstract class WebViewActivity : BaseActivity() {
    protected abstract val openInBrowserUrl: String

    private val binding by lazy {
        ActivityWebviewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        onWebViewPrepared(binding.wvMain)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_feedback, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_feedbackRefresh -> binding.wvMain.reload()
            R.id.menu_feedbackOpenInBrowser -> IntentUtils.launchUrlInBrowser(this, openInBrowserUrl)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        setSupportActionBar(binding.toolbar.tbGeneral)
        enableHomeButton()
        binding.wvMain.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    changeProgressBar(newProgress)
                    super.onProgressChanged(view, newProgress)
                }
            }
        }
    }

    override fun onBackPressed() {
        binding.wvMain.apply {
            if (canGoBack()) {
                goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    protected fun changeProgressBar(newProgress: Int) {
        binding.pbWebViewLoading.apply {
            isVisible = newProgress != 100
            progress = newProgress
        }
    }

    protected open fun onWebViewPrepared(webView: WebView) {}
}