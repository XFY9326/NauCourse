package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.constants.HTMLConst
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.beans.jwc.JwcTopic
import tool.xfy9326.naucourse.providers.contents.base.BaseNewsContent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

object TopicList : BaseNewsContent<JwcTopic>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)

    private const val JWC_TOPIC_ASPX = "TopicList.aspx"
    private val JWC_TOPIC_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_ISSUE_PATH).addPathSegment(JWC_TOPIC_ASPX).build()

    private const val ELEMENT_CLASS_TABLE_CONTENT = "TableContent"

    private const val INFO_DIVIDE_SYMBOL = "："

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(JWC_TOPIC_URL)

    override fun onRequestDetailData(url: HttpUrl): Response = (getDetailNetworkClient() as BaseLoginClient).newAutoLoginCall(url)

    override fun onBuildImageUrl(source: String): HttpUrl =
        HttpUrl.Builder().scheme(NetworkConst.HTTP).host(JwcClient.JWC_HOST).addEncodedPathSegments(
            source.replace(NetworkConst.PARENT_DIR, BaseConst.EMPTY)
        ).build()

    override fun onParseRawData(content: String): Set<JwcTopic> {
        val document = Jsoup.parse(content)
        return getJwcTopicSet(document)
    }

    private fun getJwcTopicSet(document: Document): Set<JwcTopic> {
        val tableContentElement = document.body().getElementsByClass(ELEMENT_CLASS_TABLE_CONTENT).first()
        val trElements = tableContentElement.getElementsByTag(HTMLConst.ELEMENT_TAG_TR)

        if (trElements.isEmpty()) {
            return HashSet()
        }

        val topicSet = HashSet<JwcTopic>(trElements.size)

        var title: String
        var postDate: Date
        var detailUrl: HttpUrl
        var type: String
        var clickAmount: Int

        for (i in 1 until trElements.size) {
            val tdElement = trElements[i].getElementsByTag(HTMLConst.ELEMENT_TAG_TD)

            title = tdElement[2].text()

            val urlTemp = tdElement[2].select(HTMLConst.SELECT_A_HREF_PATH_URL).first().attr(
                HTMLConst.ELEMENT_ATTR_HREF
            ).split(
                NetworkConst
                    .URL_QUERY_DIVIDE_SYMBOL
            )
            detailUrl = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(JwcClient.JWC_HOST)
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
                    PostSource.JWC,
                    newsDatum.clickAmount
                )
            )
        }
        return newsSet
    }

    override fun onParseDetailData(document: Document): GeneralNewsDetail {
        val bodyElement = document.body()

        val trElements = bodyElement.getElementsByTag(HTMLConst.ELEMENT_TAG_TABLE)[1].getElementsByTag(
            HTMLConst.ELEMENT_TAG_TR
        )
        val title = trElements[0].text()

        val detailTemp = trElements[1].text().split(BaseConst.SPACE)
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