package tool.xfy9326.naucourses.providers.contents.methods.rss

import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.contents.base.BaseRSSContent

object XgcRSS : BaseRSSContent() {
    private const val XGC_HOST = "xgc.nau.edu.cn"

    override val siteId: Int = 110
    override val templateId: Int = 181
    override val columnId: Int = 3439

    override val rssDetailServerHost: String = XGC_HOST
    override val postSource: GeneralNews.PostSource = GeneralNews.PostSource.RSS_XGC
}