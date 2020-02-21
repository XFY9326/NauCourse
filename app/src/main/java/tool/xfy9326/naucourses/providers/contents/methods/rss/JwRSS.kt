package tool.xfy9326.naucourses.providers.contents.methods.rss

import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.contents.base.BaseRSSContent

object JwRSS : BaseRSSContent() {
    override val siteId: Int = 126
    override val templateId: Int = 221
    override val columnId: Int = 4353

    override val rssDetailServerHost: String = Constants.Network.JW_HOST
    override val postSource: GeneralNews.PostSource = GeneralNews.PostSource.RSS_JW
}