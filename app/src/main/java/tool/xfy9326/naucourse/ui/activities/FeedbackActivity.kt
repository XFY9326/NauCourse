package tool.xfy9326.naucourse.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.ui.activities.base.BaseActivity
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils

class FeedbackActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        initView()
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
            R.id.menu_feedbackRefresh -> wv_feedback.reload()
            R.id.menu_feedbackOpenInBrowser -> IntentUtils.launchUrlInBrowser(this, OthersConst.ONLINE_FEEDBACK_URL)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        setSupportActionBar(tb_general)
        enableHomeButton()
        wv_feedback.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    request?.let {
                        try {
                            val url = it.url
                            if (url.scheme == NetworkConst.SCHEMA_WEIXIN) {
                                startActivity(Intent(Intent.ACTION_VIEW, url))
                                return true
                            }
                        } catch (e: Exception) {
                            return false
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    pb_feedbackLoading.apply {
                        isVisible = newProgress != 100
                        progress = newProgress
                    }
                    super.onProgressChanged(view, newProgress)
                }
            }
            postUrl(OthersConst.ONLINE_FEEDBACK_URL, getPostData().toByteArray())
        }
    }

    private fun getPostData() =
        "nickname=${getString(R.string.feedback_user_nickname)}&avatar=${getAvatarUrl()}&openid=${AccountUtils.getAccountOpenId()}&" +
                "clientVersion=${BuildConfig.VERSION_NAME}-${BuildConfig.FLAVOR}(${BuildConfig.VERSION_CODE})&os=Android&osVersion=${Build.VERSION.SDK_INT}"

    private fun getAvatarUrl() = "https://txc.gtimg.com/static/v2/img/avatar/${AccountUtils.getSpecialNumByUserId(1, 260)}.svg"

    override fun onBackPressed() {
        wv_feedback.apply {
            if (canGoBack()) {
                goBack()
            } else {
                super.onBackPressed()
            }
        }
    }
}