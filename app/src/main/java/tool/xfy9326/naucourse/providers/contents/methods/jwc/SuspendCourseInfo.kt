package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jw.SuspendCourse
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent

object SuspendCourseInfo : BaseNoParamContent<Array<SuspendCourse>>() {
    override val networkClient = getSimpleClient()

    private const val JWC_SUSPEND_COURSE_ASPX = "SuspendCourseInfo.aspx"

    private val JW_SUSPEND_COURSE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JWC_SUSPEND_COURSE_ASPX).build()

    override fun onRequestData(): Response = networkClient.newClientCall(JW_SUSPEND_COURSE_URL)

    override fun onParseData(content: String): Array<SuspendCourse> {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        return getSuspendCourseArr(bodyElement)
    }

    private fun getSuspendCourseArr(bodyElement: Element): Array<SuspendCourse> {
        TODO("Suspend Course")
    }
}