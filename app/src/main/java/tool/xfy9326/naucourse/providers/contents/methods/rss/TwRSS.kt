package tool.xfy9326.naucourse.providers.contents.methods.rss

import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.contents.base.BaseRSSContent

object TwRSS : BaseRSSContent() {
    const val TW_HOST = "tw.nau.edu.cn"

    override val siteId: Int = 105
    override val templateId: Int = 517
    override val columnId: Int = 3364

    override val rssDetailServerHost: String = TW_HOST
    override val postSource: PostSource = PostSource.RSS_TW
}