package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.ui.activities.base.WebViewActivity
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.secure.AccountUtils

class FeedbackActivity : WebViewActivity() {
    override val openInBrowserUrl: String = OthersConst.ONLINE_FEEDBACK_URL
    private val avatarUrl = "https://txc.gtimg.com/static/v2/img/avatar/${AccountUtils.getSpecialNumByUserId(1, 260)}.svg"

    override fun onWebViewPrepared(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.let {
                    try {
                        val url = it.url
                        if (url.scheme == NetworkConst.SCHEMA_WEIXIN) {
                            startActivity(Intent(Intent.ACTION_VIEW, url))
                            return true
                        }
                    } catch (e: Exception) {
                        ExceptionUtils.printStackTrace<FeedbackActivity>(e)
                        return false
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView.postUrl(OthersConst.ONLINE_FEEDBACK_URL, getPostBytes())
    }

    private fun getPostBytes() =
        ("nickname=${getString(R.string.feedback_user_nickname)}&avatar=${avatarUrl}&openid=${AccountUtils.getAccountOpenId()}&" +
                "clientVersion=${BuildConfig.VERSION_NAME}-${BuildConfig.FLAVOR}(${BuildConfig.VERSION_CODE})&os=Android&osVersion=${Build.VERSION.SDK_INT}").toByteArray()
}