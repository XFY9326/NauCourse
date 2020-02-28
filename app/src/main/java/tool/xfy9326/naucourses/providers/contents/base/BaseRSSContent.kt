package tool.xfy9326.naucourses.providers.contents.base

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.VPNClient
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourses.providers.beans.rss.RSSObject
import tool.xfy9326.naucourses.providers.contents.base.rss.NauRSSTools
import tool.xfy9326.naucourses.providers.contents.base.rss.RSSReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

abstract class BaseRSSContent : BaseNewsContent<RSSObject>() {
    protected val vpnClient = getSSOClient<VPNClient>(SSONetworkManager.ClientType.VPN)

    abstract val siteId: Int
    abstract val templateId: Int
    abstract val columnId: Int

    abstract val postSource: GeneralNews.PostSource
    protected abstract val rssDetailServerHost: String

    companion object {
        private const val ELEMENT_CLASS_WP_ARTICLE_CONTENT = "wp_articlecontent"
        private const val ELEMENT_CLASS_WP_VISIT_COUNT = "WP_VisitCount"
        private const val ELEMENT_CLASS_ARTICLE_TITLE = "Article_Title"
        private const val ELEMENT_CLASS_ARTICLE_SOURCE = "Article_Source"
        private const val ELEMENT_CLASS_ARTICLE_PUBLISH_DATE = "Article_PublishDate"
        private const val ELEMENT_CLASS_ARTI_TITLE = "arti_title"
        private const val ELEMENT_CLASS_ARTI_PUBLISHER = "arti_publisher"
        private const val ELEMENT_CLASS_ARTI_UPDATE = "arti_update"
        private const val ELEMENT_CLASS_INFO_TITLE = "infotitle"
        private const val ELEMENT_CLASS_BORDER2 = "border2"

        private const val INFO_DIVIDE_SYMBOL = "："

        private const val SELECT_TABLE_PATH = "${Constants.HTML.ELEMENT_TAG_TABLE}[class=${ELEMENT_CLASS_BORDER2}]"

        private const val ICON_URL_STR = "/_ueditor/themes/default/images/icon"

        private const val EMPTY_HTML_STR = "&nbsp;&nbsp;"

        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
    }

    final override fun onRequestData(): Response = vpnClient.newAutoLoginCall(NauRSSTools.buildRSSUrl(siteId, templateId, columnId))

    final override fun convertToGeneralNews(newsData: Set<RSSObject>): Set<GeneralNews> {
        val newsDatum = newsData.first()
        if (newsDatum.channels.isNotEmpty()) {
            val newsSet = HashSet<GeneralNews>(newsDatum.channels[0].items.size)
            for (channel in newsDatum.channels) {
                for (item in channel.items) {
                    newsSet.add(
                        GeneralNews(
                            item.title,
                            item.date,
                            item.link,
                            item.type,
                            postSource
                        )
                    )
                }
            }
            return newsSet
        } else {
            return emptySet()
        }
    }

    final override fun onParseRawData(content: String): Set<RSSObject> {
        val result = RSSReader.getRSSObject(content)
        if (result == null) {
            throw IOException("RSS Parse Result Is Empty!")
        } else {
            return setOf(result)
        }
    }

    override fun onRequestDetailData(url: HttpUrl): Response = getSimpleClient().newClientCall(url)

    final override fun onParseDetailData(content: String): GeneralNewsDetail {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()

        val title: String
        var postAdmin: String? = null
        val postDate: Date

        when (templateId) {
            181 -> {
                title = bodyElement.getElementsByClass(ELEMENT_CLASS_ARTICLE_TITLE).first().text()
                postAdmin = bodyElement.getElementsByClass(ELEMENT_CLASS_ARTICLE_SOURCE).first().text()
                postDate = readTime(bodyElement.getElementsByClass(ELEMENT_CLASS_ARTICLE_PUBLISH_DATE).first().text())!!
            }
            221 -> {
                title = bodyElement.getElementsByClass(ELEMENT_CLASS_ARTICLE_TITLE).first().text()
                postDate = readTime(bodyElement.getElementsByClass(ELEMENT_CLASS_ARTICLE_PUBLISH_DATE).first().text())!!
            }
            360 -> {
                title = bodyElement.getElementsByClass(ELEMENT_CLASS_INFO_TITLE).first().text()
                val tmp = bodyElement.select(SELECT_TABLE_PATH).first().getElementsByTag(Constants.HTML.ELEMENT_TAG_TD).first().text()
                    .split(Constants.SPACE)
                postAdmin = tmp[0].split(INFO_DIVIDE_SYMBOL)[1]
                postDate = readTime(tmp[1].split(INFO_DIVIDE_SYMBOL)[1])!!
            }
            517 -> {
                val tmpPostAdmin = bodyElement.getElementsByClass(ELEMENT_CLASS_ARTI_PUBLISHER).first().text().split(INFO_DIVIDE_SYMBOL)
                val tmpPostDate = bodyElement.getElementsByClass(ELEMENT_CLASS_ARTI_UPDATE).first().text().split(INFO_DIVIDE_SYMBOL)
                title = bodyElement.getElementsByClass(ELEMENT_CLASS_ARTI_TITLE).first().text()
                postAdmin = tmpPostAdmin[1]
                postDate = readTime(tmpPostDate[1])!!
            }
            else -> throw IllegalArgumentException("Unsupported RSS Template Id! Template Id: $templateId")
        }

        val clickAmount = bodyElement.getElementsByClass(ELEMENT_CLASS_WP_VISIT_COUNT).first().text().toInt()
        val html = getRSSDetailHtml(document)

        return GeneralNewsDetail(title, postAdmin, postDate, clickAmount, html)
    }

    private fun getRSSDetailHtml(bodyElement: Element): String {
        val contentElement = bodyElement.getElementsByClass(ELEMENT_CLASS_WP_ARTICLE_CONTENT)[0]
        contentElement.setBaseUri(HttpUrl.Builder().scheme(Constants.Network.HTTP).host(rssDetailServerHost).build().toString())

        val imgElements = contentElement.select(Constants.HTML.SELECT_IMG_PATH)
        if (imgElements != null) {
            for (imgElement in imgElements) {
                if (ICON_URL_STR in imgElement.attr(Constants.HTML.ELEMENT_ATTR_SRC)) {
                    imgElement.remove()
                }
            }
        }
        val aElements = contentElement.select(Constants.HTML.SELECT_A_HREF_PATH_URL)
        if (aElements != null) {
            for (aElement in aElements) {
                aElement.attr(Constants.HTML.ELEMENT_ATTR_HREF, aElement.absUrl(Constants.HTML.ELEMENT_ATTR_HREF))
            }
        }
        return contentElement.html().replace(EMPTY_HTML_STR, Constants.EMPTY)
    }

    // 解决SimpleDateFormat线程不安全问题
    @Synchronized
    private fun readTime(text: String) = DATE_FORMAT_YMD.parse(text)
}