package tool.xfy9326.naucourse.providers.contents.methods.rss

import okhttp3.HttpUrl
import okhttp3.Response
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.VPNClient
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.contents.base.BaseRSSContent

object XxbRSS : BaseRSSContent() {
    const val XXB_HOST = "xxb.nau.edu.cn"

    override val siteId: Int = 116
    override val templateId: Int = 360
    override val columnId: Int = 4048

    override val rssDetailServerHost: String = XXB_HOST
    override val postSource: GeneralNews.PostSource = GeneralNews.PostSource.RSS_XXB

    override fun onRequestDetailData(url: HttpUrl): Response = getLoginClient<VPNClient>(LoginNetworkManager.ClientType.VPN).newAutoLoginCall(url)
}