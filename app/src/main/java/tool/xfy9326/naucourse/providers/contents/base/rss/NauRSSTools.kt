package tool.xfy9326.naucourse.providers.contents.base.rss

import okhttp3.HttpUrl
import tool.xfy9326.naucourse.constants.NetworkConst

object NauRSSTools {
    private const val RSS_HOST = "plus.nau.edu.cn"
    private const val RSS_WP3_SERVICES_PATH = "_wp3services"
    private const val RSS_RSS_OFFER_PATH = "rssoffer"

    private const val RSS_SITE_ID_PARAM = "siteId"
    private const val RSS_TEMPLATE_ID_PARAM = "templateId"
    private const val RSS_COLUMN_ID_PARAM = "columnId"

    fun buildRSSUrl(siteId: Int, templateId: Int, columnId: Int) =
        HttpUrl.Builder().scheme(NetworkConst.HTTP).host(RSS_HOST)
            .addPathSegment(RSS_WP3_SERVICES_PATH).addPathSegment(RSS_RSS_OFFER_PATH)
            .addQueryParameter(RSS_SITE_ID_PARAM, siteId.toString()).addQueryParameter(RSS_TEMPLATE_ID_PARAM, templateId.toString())
            .addQueryParameter(RSS_COLUMN_ID_PARAM, columnId.toString()).build()
}