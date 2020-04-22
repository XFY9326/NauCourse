package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.Exam
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent
import java.text.SimpleDateFormat
import java.util.*

object MyExamArrangeList : BaseNoParamContent<Array<Exam>>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private val DATE_FORMAT_YMD_HM_CH = SimpleDateFormat(Constants.Time.FORMAT_YMD_HM_CH, Locale.CHINA)

    private const val JWC_MY_EXAM_ARRANGE_LIST_ASPX = "MyExamArrangeList.aspx"
    private const val TIME_SPLIT_SYMBOL = "-"

    private val JWC_MY_EXAM_ARRANGE_LIST_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(JWC_MY_EXAM_ARRANGE_LIST_ASPX).build()

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(JWC_MY_EXAM_ARRANGE_LIST_URL)

    override fun onParseData(content: String): Array<Exam> = getExamArr(Jsoup.parse(content).body())

    private fun getExamArr(bodyElement: Element): Array<Exam> {
        val trElements = bodyElement.getElementById(Constants.HTML.ELEMENT_ID_CONTENT).getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        val examArr = arrayOfNulls<Exam>(trElements.size - 2)

        var courseId: String
        var name: String
        var credit: Float
        var teachClass: String
        var startDate: Date
        var endDate: Date
        var location: String
        var property: String
        var type: String

        for ((arrIndex, i) in (2 until trElements.size).withIndex()) {
            val tdElements = trElements[i].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            courseId = tdElements[1].text()
            name = tdElements[2].text()
            credit = tdElements[3].text().toFloat()
            teachClass = tdElements[4].text()

            val dateStr = tdElements[5].text()
            val daySplit = dateStr.split(Constants.SPACE)
            val timeSplit = daySplit[1].split(TIME_SPLIT_SYMBOL)
            startDate = readTime("${daySplit[0]} ${timeSplit[0]}")!!
            endDate = readTime("${daySplit[0]} ${timeSplit[1]}")!!

            location = tdElements[6].text()
            property = tdElements[7].text()
            type = tdElements[8].text()

            examArr[arrIndex] = Exam(courseId, name, credit, teachClass, startDate, endDate, location, property, type)
        }

        return examArr.requireNoNulls()
    }

    // 解决SimpleDateFormat线程不安全问题
    @Synchronized
    private fun readTime(text: String) = DATE_FORMAT_YMD_HM_CH.parse(text)
}