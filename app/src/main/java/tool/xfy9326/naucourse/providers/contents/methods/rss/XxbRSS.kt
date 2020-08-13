package tool.xfy9326.naucourse.providers.contents.methods.rss

import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.contents.base.BaseRSSContent

object XxbRSS : BaseRSSContent() {
    const val XXB_HOST = "xxb.nau.edu.cn"

    override val siteId: Int = 116
    override val templateId: Int = 360
    override val columnId: Int = 4048

    override val rssDetailServerHost: String = XXB_HOST
    override val postSource: PostSource = PostSource.RSS_XXB
}