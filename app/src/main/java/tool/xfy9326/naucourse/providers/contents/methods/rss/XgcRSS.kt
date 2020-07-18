package tool.xfy9326.naucourse.providers.contents.methods.rss

import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.contents.base.BaseRSSContent

object XgcRSS : BaseRSSContent() {
    const val XGC_HOST = "xgc.nau.edu.cn"

    override val siteId: Int = 110
    override val templateId: Int = 181
    override val columnId: Int = 3439

    override val rssDetailServerHost: String = XGC_HOST
    override val postSource: PostSource = PostSource.RSS_XGC
}