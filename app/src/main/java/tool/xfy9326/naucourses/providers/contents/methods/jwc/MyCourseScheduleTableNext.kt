package tool.xfy9326.naucourses.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.JwcClient
import tool.xfy9326.naucourses.providers.beans.jwc.*
import tool.xfy9326.naucourses.providers.contents.base.BaseNoParamContent
import java.io.IOException

object MyCourseScheduleTableNext : BaseNoParamContent<CourseSet>() {
    private val jwcClient = getSSOClient<JwcClient>(SSONetworkManager.ClientType.JWC)

    private const val COURSE_TABLE_NEXT_ASPX = "MyCourseScheduleTableNext.aspx"
    private val COURSE_TABLE_NEXT_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(COURSE_TABLE_NEXT_ASPX).build()

    private const val COURSE_LOCATION_STR = "上课地点："
    private const val COURSE_TIME_STR = "上课时间："
    private const val TIMES_CHAR = '第'
    private const val COURSE_NUM_CHAR = '节'
    private const val WEEK_NUM_CHAR = '周'
    private const val WEEK_TYPE_SINGLE_CHAR = '单'
    private const val WEEK_TYPE_DOUBLE_CHAR = '双'
    private const val WEEK_TYPE_AND_CHAR = '之'
    private const val MULTI_TIME_JOIN_SYMBOL = ','
    private const val FROM_TO_TIME_JOIN_SYMBOL = '-'

    override fun onRequestData(): Response = jwcClient.newAutoLoginCall(COURSE_TABLE_NEXT_URL)

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
        var weeksArr: CharArray? = null
        var weekDay: Short
        var courseNumArr: CharArray? = null

        var rawWeeksStr: String
        var rawCourseNumStr: String

        var weekMode: WeekMode
        var weeksPeriodArray: ArrayList<TimePeriod>
        var coursesNumPeriodArray: ArrayList<TimePeriod>

        courseLocationSplit.forEach {
            weekMode = WeekMode.ALL_WEEKS
            weeksPeriodArray = ArrayList(1)
            coursesNumPeriodArray = ArrayList(1)

            val courseTimeSplit = it.split(COURSE_TIME_STR)
            location = courseTimeSplit[0].trimEnd()

            val weekStr = courseTimeSplit[1].substring(0, courseTimeSplit[1].indexOf(WEEK_NUM_CHAR) + 1)

            rawWeeksStr = if (!weekStr.startsWith(TIMES_CHAR)) {
                TIMES_CHAR + weekStr
            } else {
                weekStr
            }

            if (weekStr.startsWith(TIMES_CHAR)) {
                val weekNumStr = weekStr.substring(1, weekStr.indexOf(WEEK_NUM_CHAR))
                if (MULTI_TIME_JOIN_SYMBOL in weekNumStr) {
                    weekNumStr.split(MULTI_TIME_JOIN_SYMBOL).forEach { num ->
                        weeksArr = if (FROM_TO_TIME_JOIN_SYMBOL in num) {
                            val temp = num.split(FROM_TO_TIME_JOIN_SYMBOL)
                            val periodTemp = TimePeriod(temp[0].toInt(), temp[1].toInt())
                            weeksPeriodArray.add(periodTemp)
                            periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE)
                        } else {
                            val periodTemp = TimePeriod(num.toInt())
                            weeksPeriodArray.add(periodTemp)
                            periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE)
                        }
                    }
                } else {
                    weeksArr = if (FROM_TO_TIME_JOIN_SYMBOL in weekNumStr) {
                        val temp = weekNumStr.split(FROM_TO_TIME_JOIN_SYMBOL)
                        val periodTemp = TimePeriod(temp[0].toInt(), temp[1].toInt())
                        weeksPeriodArray.add(periodTemp)
                        periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE)
                    } else {
                        val periodTemp = TimePeriod(weekNumStr.toInt())
                        weeksPeriodArray.add(periodTemp)
                        periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE)
                    }
                }
            } else if (WEEK_TYPE_AND_CHAR in weekStr) {
                val weekNum = weekStr.subSequence(0, weekStr.indexOf(WEEK_TYPE_AND_CHAR)).split(FROM_TO_TIME_JOIN_SYMBOL)
                val periodTemp = TimePeriod(weekNum[0].toInt(), weekNum[1].toInt())
                if (WEEK_TYPE_SINGLE_CHAR in weekStr) {
                    weekMode = WeekMode.ODD_WEEK_ONLY
                    weeksArr = periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, oddMode = true)
                } else if (WEEK_TYPE_DOUBLE_CHAR in weekStr) {
                    weekMode = WeekMode.EVEN_WEEK_ONLY
                    weeksArr = periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, evenMode = true)
                }
                weeksPeriodArray.add(periodTemp)
            } else {
                val weekNum = weekStr.subSequence(0, weekStr.indexOf(WEEK_NUM_CHAR)).split(FROM_TO_TIME_JOIN_SYMBOL)
                val periodTemp = TimePeriod(weekNum[0].toInt(), weekNum[1].toInt())
                weeksPeriodArray.add(periodTemp)
                weeksArr = periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE)
            }

            weekDay = courseTimeSplit[1].substring(weekStr.length + 1, weekStr.length + 1 + 1).toShort()

            rawCourseNumStr = courseTimeSplit[1].substring(weekStr.length + 1 + 1, courseTimeSplit[1].length)

            val courseNumText = rawCourseNumStr.substring(1, rawCourseNumStr.indexOf(COURSE_NUM_CHAR))
            when {
                MULTI_TIME_JOIN_SYMBOL in courseNumText -> courseNumText.split(MULTI_TIME_JOIN_SYMBOL).forEach { num ->
                    val periodTemp = TimePeriod(num.toInt())
                    coursesNumPeriodArray.add(periodTemp)
                    courseNumArr = periodTemp.convertToCharArray(Constants.Course.MAX_COURSE_LENGTH)
                }
                FROM_TO_TIME_JOIN_SYMBOL in courseNumText -> {
                    val splitTemp = courseNumText.split(FROM_TO_TIME_JOIN_SYMBOL)
                    val periodTemp = TimePeriod(splitTemp[0].toInt(), splitTemp[1].toInt())
                    coursesNumPeriodArray.add(periodTemp)
                    courseNumArr = periodTemp.convertToCharArray(Constants.Course.MAX_COURSE_LENGTH)
                }
                else -> {
                    val periodTemp = TimePeriod(courseNumText.toInt())
                    coursesNumPeriodArray.add(periodTemp)
                    courseNumArr = periodTemp.convertToCharArray(Constants.Course.MAX_COURSE_LENGTH)
                }
            }
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