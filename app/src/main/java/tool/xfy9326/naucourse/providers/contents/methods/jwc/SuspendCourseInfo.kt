package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.constants.HTMLConst
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.SuspendCourse
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent
import tool.xfy9326.naucourse.utils.BaseUtils.insert
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object SuspendCourseInfo : BaseNoParamContent<Array<SuspendCourse>>() {
    override val networkClient = getSimpleClient()

    private const val JWC_SUSPEND_COURSE_ASPX = "SuspendCourseInfo.aspx"
    private const val TABLE_ID = "list"
    private const val ATTR_ROW_SPAN = "rowspan"

    private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)
    private val JW_SUSPEND_COURSE_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JWC_SUSPEND_COURSE_ASPX).build()

    override fun onRequestData(): Response = networkClient.newClientCall(JW_SUSPEND_COURSE_URL)

    override fun onParseData(content: String): Array<SuspendCourse> = getSuspendCourseArr(Jsoup.parse(content).body())

    private fun getSuspendCourseArr(bodyElement: Element): Array<SuspendCourse> {
        val trTags = bodyElement.getElementById(TABLE_ID).getElementsByTag(HTMLConst.ELEMENT_TAG_TR)
        if (trTags.size > 1) {
            val result = ArrayList<SuspendCourse>(trTags.size / 2)

            var name: String? = null
            var teacher: String? = null
            var teachClass: String? = null

            var resultDetail: Array<SuspendCourse.TimeDetail?>? = null
            var currentRowSpan = 1
            var currentRow = 0
            for (i in 1 until trTags.size) {
                val tdTags = trTags[i].getElementsByTag(HTMLConst.ELEMENT_TAG_TD)

                if (currentRow == 0) {
                    currentRowSpan = tdTags[0].attr(ATTR_ROW_SPAN).toIntOrNull() ?: 1
                    resultDetail = arrayOfNulls(currentRowSpan)

                    name = tdTags[1].text()
                    teachClass = tdTags[2].text()
                    teacher = tdTags[3].text()

                    resultDetail[currentRow] = SuspendCourse.TimeDetail(
                        tdTags[4].text(),
                        tdTags[5].text(),
                        tdTags[6].text(),
                        DATE_FORMAT_YMD.parse(tdTags[7].text())!!
                    )
                } else {
                    resultDetail!![currentRow] = SuspendCourse.TimeDetail(
                        tdTags[0].text(),
                        tdTags[1].text().insert(2, BaseConst.SPACE),
                        tdTags[2].text(),
                        DATE_FORMAT_YMD.parse(tdTags[3].text())!!
                    )
                }

                if (currentRow >= currentRowSpan - 1) {
                    result.add(SuspendCourse(name!!, teacher!!, teachClass!!, resultDetail.requireNoNulls()))

                    name = null
                    teacher = null
                    teachClass = null
                    currentRow = 0
                    currentRowSpan = 1
                    resultDetail = null
                } else {
                    currentRow++
                }
            }

            return result.toTypedArray()
        } else {
            return emptyArray()
        }
    }
}