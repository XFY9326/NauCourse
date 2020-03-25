package tool.xfy9326.naucourse.providers.contents.methods.rss

import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.contents.base.BaseRSSContent

object JwRSS : BaseRSSContent() {
    override val siteId: Int = 126
    override val templateId: Int = 221
    override val columnId: Int = 4353

    override val rssDetailServerHost: String = Constants.Network.JW_HOST
    override val postSource: GeneralNews.PostSource = GeneralNews.PostSource.RSS_JW
}