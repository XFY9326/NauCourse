package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.constants.HTMLConst
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomInfo
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent
import java.text.SimpleDateFormat
import java.util.*

object EmptyRoomList : BaseNoParamContent<EmptyRoomInfo>() {
    private const val PAGE_URL = "EmptyRoomList.aspx"
    private val REQUEST_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(JwcClient.JWC_HOST).addPathSegment(PAGE_URL).build()
    private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)

    private const val CAMPUS_NAME_ID = "campusName"
    private const val TERM_ID = "Term"
    private const val START_DATE_ID = "StartDate"
    private const val END_DATE_ID = "EndDate"
    private const val BJC_ID = "BJC"
    private const val EJC_ID = "EJC"

    private const val OPTION_TAG = "option"

    override val networkClient = getSimpleClient()

    override fun onRequestData(): Response = networkClient.newClientCall(REQUEST_URL)

    override fun onParseData(content: String): EmptyRoomInfo {
        val body = Jsoup.parse(content).body()

        val campusNameTag = body.getElementById(CAMPUS_NAME_ID).getElementsByTag(OPTION_TAG)
        val campusNameArr = arrayOfNulls<Pair<String, String>>(campusNameTag.size)
        for ((i, element) in campusNameTag.withIndex()) {
            campusNameArr[i] = Pair(element.attr(HTMLConst.ELEMENT_ATTR_VALUE), element.text().trim())
        }

        val term = body.getElementById(TERM_ID).text()
        val startDate = DATE_FORMAT_YMD.parse(body.getElementById(START_DATE_ID).text())
        val endDate = DATE_FORMAT_YMD.parse(body.getElementById(END_DATE_ID).text())

        val bjcTag = body.getElementById(BJC_ID).getElementsByTag(OPTION_TAG)
        val bjcArr = arrayOfNulls<EmptyRoomInfo.Time>(bjcTag.size)
        for ((i, element) in bjcTag.withIndex()) {
            bjcArr[i] = EmptyRoomInfo.Time(element.attr(HTMLConst.ELEMENT_ATTR_VALUE).toInt(), element.text().trim())
        }

        val ejcTag = body.getElementById(EJC_ID).getElementsByTag(OPTION_TAG)
        val ejcArr = arrayOfNulls<EmptyRoomInfo.Time>(ejcTag.size)
        for ((i, element) in ejcTag.withIndex()) {
            ejcArr[i] = EmptyRoomInfo.Time(element.attr(HTMLConst.ELEMENT_ATTR_VALUE).toInt(), element.text().trim())
        }

        return EmptyRoomInfo(campusNameArr.requireNoNulls(), term, startDate!!, endDate!!, bjcArr.requireNoNulls(), ejcArr.requireNoNulls())
    }
}