package tool.xfy9326.naucourse.providers.contents.methods.www

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.constants.HTMLConst
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.providers.contents.base.BaseParamContent

object SchoolCalendarImage : BaseParamContent<HttpUrl, HttpUrl>() {
    override val networkClient = getSimpleClient()

    private const val PARAM_PATH = "5825"
    private const val PAGE_HTM_PATH = "list.htm"

    private const val ELEMENT_CLASS_WP_ARTICLE_CONTENT = "wp_articlecontent"
    private const val SELECT_WP_ARTICLE_PATH =
        "${HTMLConst.ELEMENT_TAG_DIV}[${HTMLConst.ELEMENT_ATTR_CLASS}=${ELEMENT_CLASS_WP_ARTICLE_CONTENT}]"

    val CURRENT_TERM_CALENDAR_PAGE_URL = HttpUrl.Builder().scheme(NetworkConst.HTTPS).host(
        NetworkConst.NAU_HOST
    )
        .addPathSegments(PARAM_PATH).addPathSegment(PAGE_HTM_PATH).build()

    private lateinit var requestUrl: HttpUrl

    override fun onParamSet(param: HttpUrl) {
        requestUrl = param
    }

    override fun onRequestData(): Response = networkClient.newClientCall(requestUrl)

    override fun onParseData(content: String): HttpUrl {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        bodyElement.setBaseUri(
            HttpUrl.Builder().scheme(NetworkConst.HTTPS).host(
                NetworkConst.NAU_HOST
            ).build().toString()
        )
        val imgUrl = bodyElement.select(SELECT_WP_ARTICLE_PATH).first().select(HTMLConst.SELECT_IMG_PATH).first()
            .absUrl(HTMLConst.ELEMENT_ATTR_SRC)
        return imgUrl.toHttpUrl()
    }
}