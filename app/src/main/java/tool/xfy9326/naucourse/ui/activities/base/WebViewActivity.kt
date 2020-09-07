package tool.xfy9326.naucourse.ui.activities.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.utils.utility.IntentUtils

abstract class WebViewActivity : BaseActivity() {
    protected abstract val openInBrowserUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initView()
        onWebViewPrepared(wv_main)
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
            R.id.menu_feedbackRefresh -> wv_main.reload()
            R.id.menu_feedbackOpenInBrowser -> IntentUtils.launchUrlInBrowser(this, openInBrowserUrl)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        setSupportActionBar(tb_general)
        enableHomeButton()
        wv_main.apply {
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
        wv_main.apply {
            if (canGoBack()) {
                goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    protected fun changeProgressBar(newProgress: Int) {
        pb_webViewLoading.apply {
            isVisible = newProgress != 100
            progress = newProgress
        }
    }

    protected open fun onWebViewPrepared(webView: WebView) {}
}