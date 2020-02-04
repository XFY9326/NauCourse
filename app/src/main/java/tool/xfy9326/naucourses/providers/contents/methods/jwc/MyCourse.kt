package tool.xfy9326.naucourses.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.NauNetworkManager
import tool.xfy9326.naucourses.network.clients.JwcClient
import tool.xfy9326.naucourses.providers.contents.base.BaseNoParamContent
import tool.xfy9326.naucourses.providers.contents.beans.jwc.CourseScore

object MyCourse : BaseNoParamContent<Array<CourseScore>>() {
    private val jwcClient = getSSOClient<JwcClient>(NauNetworkManager.ClientType.JWC)

    private const val JWC_MY_COURSE_ASPX = "MyCourse.aspx"

    private val JWC_MY_COURSE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(JWC_MY_COURSE_ASPX).build()

    private const val NOT_ENTRY_STR = "未录入"
    private const val NOT_MEASURE_STR = "未测评"
    private const val NOT_PUBLISH_STR = "未发布"

    override fun onRequestData(): Response = jwcClient.newAutoLoginCall(JWC_MY_COURSE_URL)

    override fun onParseData(content: String): Array<CourseScore> {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        return getCourseScoreArr(bodyElement)
    }

    private fun getCourseScoreArr(bodyElement: Element): Array<CourseScore> {
        val trElements = bodyElement.getElementById(Constants.HTML.ELEMENT_ID_CONTENT).getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        val courseScoreArr = arrayOfNulls<CourseScore>(trElements.size - 2)

        var courseId: String
        var name: String
        var credit: Float
        var teachClass: String
        var type: String
        var property: String
        var notes: String
        var ordinaryGrades: Float
        var midTermGrades: Float
        var finalTermGrades: Float
        var overAllGrades: Float
        var notEntry: Boolean
        var notMeasure: Boolean
        var notPublish: Boolean

        for ((arrIndex, i) in (2 until trElements.size).withIndex()) {
            val tdElements = trElements[i].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            ordinaryGrades = CourseScore.DEFAULT_GRADES
            midTermGrades = CourseScore.DEFAULT_GRADES
            finalTermGrades = CourseScore.DEFAULT_GRADES
            overAllGrades = CourseScore.DEFAULT_GRADES

            notEntry = CourseScore.DEFAULT_ENTRY
            notMeasure = CourseScore.DEFAULT_MEASURE
            notPublish = CourseScore.DEFAULT_PUBLISH

            courseId = tdElements[1].text()
            name = tdElements[2].text()
            credit = tdElements[3].text().toFloat()
            teachClass = tdElements[4].text()

            val ordinaryGradesText = tdElements[5].text()

            when {
                NOT_ENTRY_STR in ordinaryGradesText -> {
                    notEntry = true
                }
                NOT_PUBLISH_STR in ordinaryGradesText -> {
                    notPublish = true
                }
                NOT_MEASURE_STR in ordinaryGradesText -> {
                    notMeasure = true
                }
                else -> {
                    ordinaryGrades = ordinaryGradesText.toFloat()
                    midTermGrades = tdElements[6].text().toFloat()
                    finalTermGrades = tdElements[7].text().toFloat()
                    overAllGrades = tdElements[8].text().toFloat()
                }
            }

            type = tdElements[9].text()
            property = tdElements[10].text()
            notes = tdElements[11].text()

            courseScoreArr[arrIndex] = CourseScore(
                courseId,
                name,
                credit,
                teachClass,
                type,
                property,
                notes,
                ordinaryGrades,
                midTermGrades,
                finalTermGrades,
                overAllGrades,
                notEntry,
                notMeasure,
                notPublish
            )
        }

        return courseScoreArr.requireNoNulls()
    }
}