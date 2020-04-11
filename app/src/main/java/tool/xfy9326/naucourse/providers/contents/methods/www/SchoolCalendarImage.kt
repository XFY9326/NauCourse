package tool.xfy9326.naucourse.providers.contents.methods.www

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.providers.contents.base.BaseParamContent

object SchoolCalendarImage : BaseParamContent<HttpUrl, HttpUrl>() {
    override val networkClient = getSimpleClient()

    private const val PARAM_PATH = "5825"
    private const val PAGE_HTM_PATH = "list.htm"

    private const val ELEMENT_CLASS_WP_ARTICLE_CONTENT = "wp_articlecontent"
    private const val SELECT_WP_ARTICLE_PATH =
        "${Constants.HTML.ELEMENT_TAG_DIV}[${Constants.HTML.ELEMENT_ATTR_CLASS}=${ELEMENT_CLASS_WP_ARTICLE_CONTENT}]"

    val CURRENT_TERM_CALENDAR_PAGE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTPS).host(Constants.Network.NAU_HOST)
        .addPathSegments(PARAM_PATH).addPathSegment(PAGE_HTM_PATH).build()

    private lateinit var requestUrl: HttpUrl

    override fun onParamSet(param: HttpUrl) {
        requestUrl = param
    }

    override fun onRequestData(): Response = networkClient.newClientCall(requestUrl)

    override fun onParseData(content: String): HttpUrl {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        bodyElement.setBaseUri(HttpUrl.Builder().scheme(Constants.Network.HTTPS).host(Constants.Network.NAU_HOST).build().toString())
        val imgUrl = bodyElement.select(SELECT_WP_ARTICLE_PATH).first().select(Constants.HTML.SELECT_IMG_PATH).first()
            .absUrl(Constants.HTML.ELEMENT_ATTR_SRC)
        return imgUrl.toHttpUrl()
    }
}