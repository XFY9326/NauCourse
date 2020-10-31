package tool.xfy9326.naucourse.providers.contents.base

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.constants.HTMLConst
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.NGXClient
import tool.xfy9326.naucourse.network.clients.base.BaseNetworkClient
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.beans.rss.RSSObject
import tool.xfy9326.naucourse.providers.contents.base.rss.NauRSSTools
import tool.xfy9326.naucourse.providers.contents.base.rss.RSSReader
import tool.xfy9326.naucourse.providers.contents.methods.rss.TwRSS
import tool.xfy9326.naucourse.providers.contents.methods.rss.XgcRSS
import tool.xfy9326.naucourse.providers.contents.methods.rss.XxbRSS
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

abstract class BaseRSSContent : BaseNewsContent<RSSObject>() {
    override val networkClient = getLoginClient<NGXClient>(LoginNetworkManager.ClientType.NGX)

    abstract val siteId: Int
    abstract val templateId: Int
    abstract val columnId: Int

    abstract val postSource: PostSource
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

        private const val SELECT_TABLE_PATH = "${HTMLConst.ELEMENT_TAG_TABLE}[class=${ELEMENT_CLASS_BORDER2}]"

        private const val ICON_URL_STR = "/_ueditor/themes/default/images/icon"

        private const val EMPTY_HTML_STR = "&nbsp;&nbsp;"

        private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)
    }

    override fun getDetailNetworkClient(): BaseNetworkClient = getSimpleClient()

    final override fun onRequestData(): Response = networkClient.newAutoLoginCall(NauRSSTools.buildRSSUrl(siteId, templateId, columnId))

    override fun onBuildImageUrl(source: String): HttpUrl =
        HttpUrl.Builder().scheme(NetworkConst.HTTP).host(getHost()).addEncodedPathSegments(
            if (source.startsWith(NetworkConst.DIR)) source.substring(1) else source
        ).build()

    private fun getHost(): String =
        when (postSource) {
            PostSource.RSS_JW -> NetworkConst.JW_HOST
            PostSource.RSS_TW -> TwRSS.TW_HOST
            PostSource.RSS_XGC -> XgcRSS.XGC_HOST
            PostSource.RSS_XXB -> XxbRSS.XXB_HOST
            else -> error("Incorrect RSS Post Source $postSource")
        }

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
            throw IOException("RSS Parse Result is Empty!")
        } else {
            return setOf(result)
        }
    }

    override fun onRequestDetailData(url: HttpUrl): Response = getSimpleClient().newClientCall(url)

    final override fun onParseDetailData(document: Document): GeneralNewsDetail {
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
                val tmp = bodyElement.select(SELECT_TABLE_PATH).first().getElementsByTag(HTMLConst.ELEMENT_TAG_TD).first().text()
                    .split(BaseConst.SPACE)
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
            else -> throw IllegalArgumentException("Unsupported RSS Template ID! Template ID: $templateId")
        }

        val clickAmount = bodyElement.getElementsByClass(ELEMENT_CLASS_WP_VISIT_COUNT).first().text().toInt()
        val html = getRSSDetailHtml(document)

        return GeneralNewsDetail(title, postAdmin, postDate, clickAmount, html)
    }

    private fun getRSSDetailHtml(bodyElement: Element): String {
        val contentElement = bodyElement.getElementsByClass(ELEMENT_CLASS_WP_ARTICLE_CONTENT)[0]
        contentElement.setBaseUri(HttpUrl.Builder().scheme(NetworkConst.HTTP).host(rssDetailServerHost).build().toString())

        val imgElements = contentElement.select(HTMLConst.SELECT_IMG_PATH)
        if (imgElements != null) {
            for (imgElement in imgElements) {
                if (ICON_URL_STR in imgElement.attr(HTMLConst.ELEMENT_ATTR_SRC)) {
                    imgElement.remove()
                }
            }
        }
        val aElements = contentElement.select(HTMLConst.SELECT_A_HREF_PATH_URL)
        if (aElements != null) {
            for (aElement in aElements) {
                aElement.attr(HTMLConst.ELEMENT_ATTR_HREF, aElement.absUrl(HTMLConst.ELEMENT_ATTR_HREF))
            }
        }
        return contentElement.html().replace(EMPTY_HTML_STR, BaseConst.EMPTY)
    }

    // 解决SimpleDateFormat线程不安全问题
    @Synchronized
    private fun readTime(text: String) = DATE_FORMAT_YMD.parse(text)
}