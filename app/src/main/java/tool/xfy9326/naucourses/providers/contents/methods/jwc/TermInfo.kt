package tool.xfy9326.naucourses.providers.contents.methods.jwc

import okhttp3.Response
import org.jsoup.Jsoup
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.JwcClient
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.providers.contents.base.BaseNoParamContent

object TermInfo : BaseNoParamContent<TermDate>() {
    private val jwcClient = getSSOClient<JwcClient>(SSONetworkManager.ClientType.JWC)

    private const val ELEMENT_ID_TERM_INFO = "TermInfo"

    private const val IN_VACATION_WEEK_NUM = 0
    private const val IN_VACATION_STR = "放假中"

    override fun onRequestData(): Response = jwcClient.requestJwcMainContent()

    override fun onParseData(content: String): TermDate {
        val document = Jsoup.parse(content)
        val spanElements = document.body().getElementById(ELEMENT_ID_TERM_INFO).getElementsByTag(Constants.HTML.ELEMENT_TAG_SPAN)

        val weekText = spanElements[2].text().trim()
        val currentWeek = if (IN_VACATION_STR == weekText) {
            IN_VACATION_WEEK_NUM
        } else {
            weekText.substring(1, weekText.length - 1).toInt()
        }
        val startDate = Constants.Time.DATE_FORMAT_YMD.parse(spanElements[3].text())!!
        val endDate = Constants.Time.DATE_FORMAT_YMD.parse(spanElements[4].text())!!

        return TermDate(currentWeek, startDate, endDate)
    }
}