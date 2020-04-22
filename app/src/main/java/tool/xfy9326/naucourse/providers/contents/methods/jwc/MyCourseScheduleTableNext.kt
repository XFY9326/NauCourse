package tool.xfy9326.naucourse.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.*
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent
import java.io.IOException

object MyCourseScheduleTableNext : BaseNoParamContent<CourseSet>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private const val COURSE_TABLE_NEXT_ASPX = "MyCourseScheduleTableNext.aspx"
    private val COURSE_TABLE_NEXT_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(COURSE_TABLE_NEXT_ASPX).build()

    private const val COURSE_LOCATION_STR = "上课地点："
    private const val COURSE_TIME_STR = "上课时间："
    private const val COURSE_NUM_CHAR = '节'
    private const val WEEK_NUM_CHAR = '周'

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(COURSE_TABLE_NEXT_URL)

    override fun onParseData(content: String): CourseSet {
        val document = Jsoup.parse(content)
        return getCourseSet(document)
    }

    private fun getCourseSet(document: Document): CourseSet {
        val contentElement = document.getElementById(Constants.HTML.ELEMENT_ID_CONTENT)
        val trElements = contentElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

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
            val tdElements = trElements[tr].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

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
                    8 -> timeSet = getCourseTimeSet(id!!, tdElements[td])
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

    private fun getCourseTimeSet(courseId: String, element: Element): HashSet<CourseTime> {
        val text = element.text().trim()

        val courseLocationSplit = text.split(COURSE_LOCATION_STR).filter {
            it.isNotEmpty()
        }

        val courseTimeSet = HashSet<CourseTime>(courseLocationSplit.size)

        var location: String
        var weeksArr: CharArray?
        var weekDay: Short
        var courseNumArr: CharArray?

        var rawWeeksStr: String
        var rawCourseNumStr: String

        var weekMode: WeekMode
        var weeksPeriodArray: ArrayList<TimePeriod>
        var coursesNumPeriodArray: ArrayList<TimePeriod>

        courseLocationSplit.forEach {
            weeksPeriodArray = ArrayList(1)
            coursesNumPeriodArray = ArrayList(1)

            val courseTimeSplit = it.split(COURSE_TIME_STR)
            location = courseTimeSplit[0].trimEnd()

            val weekStr = courseTimeSplit[1].substring(0, courseTimeSplit[1].indexOf(WEEK_NUM_CHAR) + 1)

            rawWeeksStr = MyCourseScheduleTable.rawWeekStrAnalyse(weekStr)

            MyCourseScheduleTable.weekNumAnalyse(weekStr, weeksPeriodArray).let { data ->
                weeksArr = data.first
                weekMode = data.second
            }

            weekDay = courseTimeSplit[1].substring(weekStr.length + 1, weekStr.length + 1 + 1).toShort()

            rawCourseNumStr = courseTimeSplit[1].substring(weekStr.length + 1 + 1, courseTimeSplit[1].length)

            val courseNumText = rawCourseNumStr.substring(1, rawCourseNumStr.indexOf(COURSE_NUM_CHAR))
            courseNumArr = MyCourseScheduleTable.courseNumAnalyse(courseNumText, coursesNumPeriodArray)

            courseTimeSet.add(
                CourseTime(
                    courseId,
                    location,
                    String(weeksArr!!),
                    weekMode,
                    TimePeriodList(weeksPeriodArray.toTypedArray()),
                    rawWeeksStr,
                    weekDay,
                    String(courseNumArr!!),
                    TimePeriodList(coursesNumPeriodArray.toTypedArray()),
                    rawCourseNumStr
                )
            )
        }

        return courseTimeSet
    }
}