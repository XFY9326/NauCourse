package tool.xfy9326.naucourse.providers.contents.methods.www

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.CalendarItem
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent

object SchoolCalendarList : BaseNoParamContent<Array<CalendarItem>>() {
    override val networkClient = getSimpleClient()

    private const val PARAM_PATH = "p141c89"
    private const val LIST_HTM_PATH = "list.htm"

    private const val ELEMENT_CLASS_COL_TITLE = "cols_title"
    private const val SELECT_CALENDAR_PAGE_PATH =
        "${Constants.HTML.ELEMENT_TAG_SPAN}[${Constants.HTML.ELEMENT_ATTR_CLASS}=${ELEMENT_CLASS_COL_TITLE}]"

    private val CALENDAR_LIST_PAGE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTPS).host(Constants.Network.NAU_HOST)
        .addPathSegments(PARAM_PATH).addPathSegment(LIST_HTM_PATH).build()

    override fun onRequestData(): Response = networkClient.newClientCall(CALENDAR_LIST_PAGE_URL)

    override fun onParseData(content: String): Array<CalendarItem> {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        bodyElement.setBaseUri(HttpUrl.Builder().scheme(Constants.Network.HTTPS).host(Constants.Network.NAU_HOST).build().toString())
        return getCalendarListMap(bodyElement)
    }

    private fun getCalendarListMap(bodyElement: Element): Array<CalendarItem> {
        val spanElements = bodyElement.select(SELECT_CALENDAR_PAGE_PATH)

        if (spanElements.isEmpty()) {
            return emptyArray()
        }

        val calendarMap = arrayOfNulls<CalendarItem>(spanElements.size)

        for ((i, spanElement) in spanElements.withIndex()) {
            val aElement = spanElement.select(Constants.HTML.SELECT_A_HREF_PATH_URL).first()
            calendarMap[i] = CalendarItem(aElement.text(), aElement.absUrl(Constants.HTML.ELEMENT_ATTR_HREF).toHttpUrl())
        }

        return calendarMap.requireNoNulls()
    }
}