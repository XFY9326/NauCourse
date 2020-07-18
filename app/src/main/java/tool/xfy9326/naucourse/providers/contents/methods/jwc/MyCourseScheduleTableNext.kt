package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tool.xfy9326.naucourse.constants.HTMLConst
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.providers.beans.jwc.Term
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent
import java.io.IOException

object MyCourseScheduleTableNext : BaseNoParamContent<CourseSet>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private const val COURSE_TABLE_NEXT_ASPX = "MyCourseScheduleTableNext.aspx"
    private val COURSE_TABLE_NEXT_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(COURSE_TABLE_NEXT_ASPX).build()

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(COURSE_TABLE_NEXT_URL)

    override fun onParseData(content: String): CourseSet {
        val document = Jsoup.parse(content)
        return getCourseSet(document)
    }

    private fun getCourseSet(document: Document): CourseSet {
        val contentElement = document.getElementById(HTMLConst.ELEMENT_ID_CONTENT)
        val trElements = contentElement.getElementsByTag(HTMLConst.ELEMENT_TAG_TR)

        val courseSet = HashSet<Course>(trElements.size)

        var id: String? = null
        var name: String? = null
        var teacher: String? = null
        var teachClass: String? = null
        var credit: Float? = null
        var type: String? = null
        var property: String? = null
        var timeSet: HashSet<CourseTime>? = null
        var termStr: String? = null

        for (tr in 2 until trElements.size) {
            val tdElements = trElements[tr].getElementsByTag(HTMLConst.ELEMENT_TAG_TD)

            if (tdElements.size < 9) {
                throw IOException("Incomplete Course Data!")
            }

            for (td in 1 until tdElements.size) {
                when (td) {
                    1 -> id = tdElements[td].text()
                    2 -> name = tdElements[td].text()
                    3 -> credit = tdElements[td].text().toFloat()
                    4 -> teachClass = tdElements[td].text()
                    5 -> property = tdElements[td].text()
                    6 -> type = tdElements[td].text()
                    7 -> teacher = tdElements[td].text()
                    8 -> timeSet = MyCourseScheduleTable.getCourseTimeSet(id!!, tdElements[td], true)
                    9 -> if (termStr == null) termStr = tdElements[td].text()
                }
            }

            courseSet.add(
                Course(
                    id!!,
                    name!!,
                    teacher!!,
                    teachClass!!,
                    credit!!,
                    type!!,
                    property!!,
                    timeSet!!
                )
            )
        }

        return CourseSet(courseSet, Term.parse(termStr!!))
    }
}