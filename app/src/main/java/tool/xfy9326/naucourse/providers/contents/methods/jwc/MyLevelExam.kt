package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourse.providers.beans.jwc.Term
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent

object MyLevelExam : BaseNoParamContent<Array<LevelExam>>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private const val JWC_MY_LEVEL_EXAM_ASPX = "MyLevelExam.aspx"
    private const val ELEMENT_ID_TABLE2 = "Table2"

    private val JWC_MY_COURSE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(JWC_MY_LEVEL_EXAM_ASPX).build()

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(JWC_MY_COURSE_URL)

    override fun onParseData(content: String): Array<LevelExam> {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        return getLevelExamArr(bodyElement)
    }

    private fun getLevelExamArr(bodyElement: Element): Array<LevelExam> {
        val trElements = bodyElement.getElementById(ELEMENT_ID_TABLE2).getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        val levelExamArr = arrayOfNulls<LevelExam>(trElements.size - 2)

        var type: String
        var name: String
        var grade1: Float?
        var grade2: String
        var term: Term
        var ticketNum: String
        var certificateNum: String
        var notes: String

        for ((arrIndex, i) in (2 until trElements.size).withIndex()) {
            val tdElements = trElements[i].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            type = tdElements[1].text()
            name = tdElements[2].text()
            val grade1Text = tdElements[3].text()
            grade1 = if (grade1Text.isNotEmpty()) {
                grade1Text.toFloat()
            } else {
                null
            }
            grade2 = tdElements[4].text()
            term = Term.parse(tdElements[5].text())
            ticketNum = tdElements[6].text()
            certificateNum = tdElements[7].text()
            notes = tdElements[8].text()

            levelExamArr[arrIndex] = LevelExam(type, name, grade1, grade2, term, ticketNum, certificateNum, notes)
        }

        return levelExamArr.requireNoNulls()
    }
}