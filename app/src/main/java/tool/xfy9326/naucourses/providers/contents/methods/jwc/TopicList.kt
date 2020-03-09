package tool.xfy9326.naucourses.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.JwcClient
import tool.xfy9326.naucourses.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourses.providers.beans.jwc.JwcTopic
import tool.xfy9326.naucourses.providers.contents.base.BaseNewsContent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

object TopicList : BaseNewsContent<JwcTopic>() {
    override val networkClient = getSSOClient<JwcClient>(SSONetworkManager.ClientType.JWC)

    private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)

    private const val JWC_TOPIC_ASPX = "TopicList.aspx"
    private val JWC_TOPIC_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_ISSUE_PATH).addPathSegment(JWC_TOPIC_ASPX).build()

    private const val ELEMENT_CLASS_TABLE_CONTENT = "TableContent"

    private const val INFO_DIVIDE_SYMBOL = "："

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(JWC_TOPIC_URL)

    override fun onRequestDetailData(url: HttpUrl): Response = (getDetailNetworkClient() as BaseLoginClient).newAutoLoginCall(url)

    override fun onBuildImageUrl(source: String): HttpUrl =
        HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST).addEncodedPathSegments(
            source.replace(Constants.Network.PARENT_DIR, Constants.EMPTY)
        ).build()

    override fun onParseRawData(content: String): Set<JwcTopic> {
        val document = Jsoup.parse(content)
        return getJwcTopicSet(document)
    }

    private fun getJwcTopicSet(document: Document): Set<JwcTopic> {
        val tableContentElement = document.body().getElementsByClass(ELEMENT_CLASS_TABLE_CONTENT).first()
        val trElements = tableContentElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        val topicSet = HashSet<JwcTopic>(trElements.size)

        var title: String
        var postDate: Date
        var detailUrl: HttpUrl
        var type: String
        var clickAmount: Int

        for (i in 1 until trElements.size) {
            val tdElement = trElements[i].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            title = tdElement[2].text()

            val urlTemp = tdElement[2].select(Constants.HTML.SELECT_A_HREF_PATH_URL).first().attr(Constants.HTML.ELEMENT_ATTR_HREF).split(
                Constants.Network
                    .URL_QUERY_DIVIDE_SYMBOL
            )
            detailUrl = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
                .addPathSegment(JwcClient.JWC_ISSUE_PATH).addEncodedPathSegment(urlTemp[0]).query(urlTemp[1]).build()

            postDate = readTime(tdElement[3].text())!!
            clickAmount = tdElement[5].text().toInt()
            type = tdElement[6].text()

            topicSet.add(
                JwcTopic(
                    title,
                    postDate,
                    detailUrl,
                    type,
                    clickAmount
                )
            )
        }

        return topicSet
    }

    override fun convertToGeneralNews(newsData: Set<JwcTopic>): Set<GeneralNews> {
        val newsSet = HashSet<GeneralNews>(newsData.size)
        for (newsDatum in newsData) {
            newsSet.add(
                GeneralNews(
                    newsDatum.title,
                    newsDatum.postDate,
                    newsDatum.detailUrl,
                    newsDatum.type,
                    GeneralNews.PostSource.JWC,
                    newsDatum.clickAmount
                )
            )
        }
        return newsSet
    }

    override fun onParseDetailData(document: Document): GeneralNewsDetail {
        val bodyElement = document.body()

        val trElements = bodyElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TABLE)[1].getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)
        val title = trElements[0].text()

        val detailTemp = trElements[1].text().split(Constants.SPACE)
        val postAdmin = detailTemp[0].split(INFO_DIVIDE_SYMBOL)[1]
        val postDate = readTime(detailTemp[1].split(INFO_DIVIDE_SYMBOL)[1])!!
        val clickAmount = detailTemp[2].split(INFO_DIVIDE_SYMBOL)[1].toInt()

        val html = trElements[2].html()

        return GeneralNewsDetail(title, postAdmin, postDate, clickAmount, html)
    }

    // 解决SimpleDateFormat线程不安全问题
    @Synchronized
    private fun readTime(text: String) = DATE_FORMAT_YMD.parse(text)
}