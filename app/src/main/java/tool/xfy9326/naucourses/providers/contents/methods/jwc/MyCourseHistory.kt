package tool.xfy9326.naucourses.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.NauNetworkManager
import tool.xfy9326.naucourses.network.clients.JwcClient
import tool.xfy9326.naucourses.providers.contents.base.BaseNoParamContent
import tool.xfy9326.naucourses.providers.contents.beans.jwc.CourseHistory
import tool.xfy9326.naucourses.providers.contents.beans.jwc.Term

object MyCourseHistory : BaseNoParamContent<Array<CourseHistory>>() {
    private val jwcClient = getSSOClient<JwcClient>(NauNetworkManager.ClientType.JWC)

    private const val JWC_MY_COURSE_HISTORY_ASPX = "MyCourseHistory.aspx"
    private const val ELEMENT_ID_MAJOR_APPLY_LIST = "MajorApplyList"

    private val JWC_MY_COURSE_HISTORY_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(JWC_MY_COURSE_HISTORY_ASPX).build()

    override fun onRequestData(): Response = jwcClient.newAutoLoginCall(JWC_MY_COURSE_HISTORY_URL)

    override fun onParseData(content: String): Array<CourseHistory> {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        return getCourseHistoryArr(bodyElement)
    }

    private fun getCourseHistoryArr(bodyElement: Element): Array<CourseHistory> {
        val trElements = bodyElement.getElementById(ELEMENT_ID_MAJOR_APPLY_LIST).getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        val courseHistoryArr = arrayOfNulls<CourseHistory>(trElements.size - 2)

        var courseId: String
        var name: String
        var credit: Float
        var score: Float?
        var scoreRawText: String
        var creditWeight: Float
        var term: Term
        var courseProperty: String
        var academicProperty: String
        var type: String
        var notes: String

        for ((arrIndex, i) in (2 until trElements.size - 1).withIndex()) {
            val tdElements = trElements[i].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            courseId = tdElements[1].text()
            name = tdElements[2].text()
            credit = tdElements[3].text().toFloat()
            scoreRawText = tdElements[4].text()
            score = scoreRawText.toFloatOrNull()
            creditWeight = tdElements[5].text().toFloat()
            term = Term.parse(tdElements[6].text())
            courseProperty = tdElements[7].text()
            academicProperty = tdElements[8].text()
            type = tdElements[9].text()
            notes = tdElements[10].text()

            courseHistoryArr[arrIndex] = CourseHistory(
                courseId,
                name,
                credit,
                score,
                scoreRawText,
                creditWeight,
                term,
                courseProperty,
                academicProperty,
                type,
                notes
            )
        }

        return courseHistoryArr.requireNoNulls()
    }
}