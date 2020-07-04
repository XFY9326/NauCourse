package tool.xfy9326.naucourse.providers.contents.methods.alstu

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.AlstuClient
import tool.xfy9326.naucourse.network.clients.VPNClient
import tool.xfy9326.naucourse.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourse.network.tools.VPNTools
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.providers.beans.alstu.AlstuMessage
import tool.xfy9326.naucourse.providers.contents.base.BaseNewsContent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

object DefaultMessage : BaseNewsContent<AlstuMessage>() {
    override val networkClient = getLoginClient<AlstuClient>(LoginNetworkManager.ClientType.ALSTU)

    private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)

    private const val ALSTU_MESSAGE_PATH = "MESSAGE"
    private const val ALSTU_DEFAULT_ASPX = "DEFAULT.ASPX"
    private const val ALSTU_ALDFDNF_ASPX = "aldfdnf.aspx"

    private const val URL_PARAM_NAME_LX = "lx"
    private const val URL_PARAM_VALUE_ST = "st"
    private const val URL_PARAM_NAME_YLX = "ylx"
    private const val URL_PARAM_NAME_FILE = "file"

    private val ALSTU_MESSAGE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(AlstuClient.ALSTU_HOST)
        .addPathSegment(ALSTU_MESSAGE_PATH).addPathSegment(ALSTU_DEFAULT_ASPX).build()

    private const val ELEMENT_ID_MY_DATA_GRID = "MyDataGrid"
    private const val ELEMENT_ID_MY_DATA_LIST = "MyDataList"
    private const val ELEMENT_ID_TJSJ = "tjsj"
    private const val ELEMENT_ID_YDCS = "ydcs"
    private const val ELEMENT_ID_DW = "dw"
    private const val URL_FRONT_PAGE = "../"

    private const val ELEMENT_ID_NR = "nr"
    private const val ELEMENT_ID_BT = "bt"
    private const val ELEMENT_ATTR_ON_CLICK = "onclick"

    private const val FILE_DOWNLOAD_NAME_SYMBOL = "\'"

    private const val SELECT_A_ON_CLICK_PATH_URL = "${Constants.HTML.ELEMENT_TAG_A}[$ELEMENT_ATTR_ON_CLICK]"

    private const val FILE_DOWNLOAD_TITLE_HTML = "<br/><br/><br/><p>附件：</p>"

    override fun onRequestData(): Response = networkClient.newAlstuNoIndexCall(
        Request.Builder().header(Constants.Network.HEADER_REFERER, AlstuClient.ALSTU_INDEX_URL.toString()).url(ALSTU_MESSAGE_URL).build()
    )

    override fun onRequestDetailData(url: HttpUrl): Response = (getDetailNetworkClient() as BaseLoginClient).newAutoLoginCall(
        Request.Builder().header(Constants.Network.HEADER_REFERER, ALSTU_MESSAGE_URL.toString()).url(url).build()
    )

    override fun onBuildImageUrl(source: String): HttpUrl =
        HttpUrl.Builder().scheme(Constants.Network.HTTP).host(AlstuClient.ALSTU_HOST).addEncodedPathSegments(
            if (source.startsWith(Constants.Network.DIR)) source.substring(1) else source
        ).build()

    override fun onParseRawData(content: String): Set<AlstuMessage> = getAlstuMessageSet(Jsoup.parse(content))

    private fun getAlstuMessageSet(document: Document): Set<AlstuMessage> {
        val dataGridElement = document.getElementById(ELEMENT_ID_MY_DATA_GRID)
        val trElements = dataGridElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        if (trElements.isEmpty()) {
            return HashSet()
        }

        val msgSet = HashSet<AlstuMessage>(trElements.size)

        var title: String
        var url: HttpUrl
        var date: Date

        for (i in 0 until trElements.size - 1) {
            val tdElements = trElements[i].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            val urlTemp = tdElements[0].select(Constants.HTML.SELECT_A_HREF_PATH_URL).first().attr(Constants.HTML.ELEMENT_ATTR_HREF)
            url = if (urlTemp.startsWith(URL_FRONT_PAGE)) {
                val queryTemp = urlTemp.substring(URL_FRONT_PAGE.length).split(Constants.Network.URL_QUERY_DIVIDE_SYMBOL)
                HttpUrl.Builder().scheme(Constants.Network.HTTP).host(AlstuClient.ALSTU_HOST)
                    .addPathSegment(ALSTU_MESSAGE_PATH).addEncodedPathSegments(queryTemp[0]).query(queryTemp[1]).build()
            } else {
                urlTemp.toHttpUrl()
            }

            title = tdElements[0].text()
            date = readTime(tdElements[1].text())!!

            msgSet.add(AlstuMessage(title, url, date))
        }

        return msgSet
    }

    override fun convertToGeneralNews(newsData: Set<AlstuMessage>): Set<GeneralNews> {
        val newsSet = HashSet<GeneralNews>(newsData.size)
        for (newsDatum in newsData) {
            newsSet.add(
                GeneralNews(
                    newsDatum.title,
                    newsDatum.date,
                    newsDatum.url,
                    GeneralNews.PostSource.ALSTU
                )
            )
        }
        return newsSet
    }

    override fun onParseDetailData(document: Document): GeneralNewsDetail {
        val isUsingVPN = VPNClient.isPageUsingVPN(document.html())
        val bodyElement = document.body()

        val title = bodyElement.getElementById(ELEMENT_ID_BT).text().trim()
        val postAdmin = bodyElement.getElementById(ELEMENT_ID_DW).text()
        val postDate = readTime(bodyElement.getElementById(ELEMENT_ID_TJSJ).text())!!
        val clickAmount = bodyElement.getElementById(ELEMENT_ID_YDCS).text().toInt()
        val html = getAlstuDetailContent(document, isUsingVPN, postDate)

        return GeneralNewsDetail(title, postAdmin, postDate, clickAmount, html)
    }

    private fun getAlstuDetailContent(bodyElement: Element, isUsingVPN: Boolean, postDate: Date): String {
        val nrElements = bodyElement.getElementById(ELEMENT_ID_NR)
        return nrElements.outerHtml() + addDownloadUrlHtml(bodyElement, isUsingVPN, postDate)
    }

    private fun addDownloadUrlHtml(body: Element, isUsingVPN: Boolean, postDate: Date): String {
        val textBuilder = StringBuilder()
        val dataList = body.getElementById(ELEMENT_ID_MY_DATA_LIST)
        if (dataList != null) {
            textBuilder.append(FILE_DOWNLOAD_TITLE_HTML)

            val tdElements = body.getElementById(ELEMENT_ID_MY_DATA_LIST).getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            val calendar = Calendar.getInstance()
            calendar.time = postDate
            val year = calendar.get(Calendar.YEAR).toString()

            for (tdElement in tdElements) {
                val aElement = tdElement.select(SELECT_A_ON_CLICK_PATH_URL).first()

                var fileDownloadName = aElement.attr(ELEMENT_ATTR_ON_CLICK)
                fileDownloadName = fileDownloadName.substring(
                    fileDownloadName.indexOf(FILE_DOWNLOAD_NAME_SYMBOL) + 1,
                    fileDownloadName.lastIndexOf(FILE_DOWNLOAD_NAME_SYMBOL)
                )
                var downloadUrl = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(AlstuClient.ALSTU_HOST).addPathSegment(ALSTU_ALDFDNF_ASPX)
                    .addQueryParameter(URL_PARAM_NAME_LX, URL_PARAM_VALUE_ST).addQueryParameter(URL_PARAM_NAME_YLX, year)
                    .addEncodedQueryParameter(URL_PARAM_NAME_FILE, fileDownloadName).build()
                if (isUsingVPN) {
                    downloadUrl = VPNTools.buildVPNUrl(downloadUrl)
                }
                textBuilder.append(buildDownloadHtml(downloadUrl, aElement.text()))
            }
        }
        return textBuilder.toString()
    }

    private fun buildDownloadHtml(url: HttpUrl, fileName: String) = "<br/><p><a href=\"$url\">$fileName</a></p>"

    // 解决SimpleDateFormat线程不安全问题
    @Synchronized
    private fun readTime(text: String) = DATE_FORMAT_YMD.parse(text)
}