package tool.xfy9326.naucourse.ui.activities

import android.webkit.WebView
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.ui.activities.base.WebViewActivity

class SchoolBusActivity : WebViewActivity() {
    override val openInBrowserUrl: String = OthersConst.SCHOOL_BUS_STATUS_URL

    override fun onWebViewPrepared(webView: WebView) {
        webView.loadUrl(OthersConst.SCHOOL_BUS_STATUS_URL)
    }
}