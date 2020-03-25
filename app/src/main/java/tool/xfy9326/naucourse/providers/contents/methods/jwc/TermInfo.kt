package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.Response
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent
import java.text.SimpleDateFormat
import java.util.*

object TermInfo : BaseNoParamContent<TermDate>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)

    private const val ELEMENT_ID_TERM_INFO = "TermInfo"

    private const val IN_VACATION_WEEK_NUM = 0
    private const val IN_VACATION_STR = "放假中"

    override fun onRequestData(): Response = networkClient.requestJwcMainContent()

    override fun onParseData(content: String): TermDate {
        val document = Jsoup.parse(content)
        val spanElements = document.body().getElementById(ELEMENT_ID_TERM_INFO).getElementsByTag(Constants.HTML.ELEMENT_TAG_SPAN)

        val weekText = spanElements[2].text().trim()
        val currentWeek = if (IN_VACATION_STR == weekText) {
            IN_VACATION_WEEK_NUM
        } else {
            weekText.substring(1, weekText.length - 1).toInt()
        }
        val startDate = readTime(spanElements[3].text())!!
        val endDate = readTime(spanElements[4].text())!!

        return TermDate(currentWeek, startDate, endDate)
    }

    // 解决SimpleDateFormat线程不安全问题
    @Synchronized
    private fun readTime(text: String) = DATE_FORMAT_YMD.parse(text)
}