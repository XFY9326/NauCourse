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
import java.util.*
import kotlin.collections.HashSet

object MyCourseScheduleTable : BaseNoParamContent<CourseSet>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private const val COURSE_TABLE_ASPX = "MyCourseScheduleTable.aspx"
    private val COURSE_TABLE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(COURSE_TABLE_ASPX).build()

    private const val STUDY_CHAR = '学'
    private const val ACADEMIC_YEAR_JOIN_SYMBOL = '-'
    private const val TIMES_CHAR = '第'
    private const val FIRST_CHAR = '一'
    private const val SECOND_CHAR = '二'
    private const val COURSE_LOCATION_STR = "上课地点："
    private const val COURSE_TIME_STR = "上课时间："
    private const val COURSE_TIME_JOIN_SYMBOL = " "
    private const val COURSE_NUM_CHAR = '节'
    private const val WEEK_NUM_CHAR = '周'
    private const val WEEK_TYPE_SINGLE_CHAR = '单'
    private const val WEEK_TYPE_DOUBLE_CHAR = '双'
    private const val WEEK_TYPE_AND_CHAR = '之'
    private const val MULTI_TIME_JOIN_SYMBOL = ','
    private const val FROM_TO_TIME_JOIN_SYMBOL = '-'

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(COURSE_TABLE_URL)

    override fun onParseData(content: String): CourseSet {
        val document = Jsoup.parse(content)
        val term = getTerm(document)
        val courseSet = getCourseSet(document)
        return CourseSet(courseSet, term)
    }

    private fun getTerm(document: Document): Term {
        val termElement = document.body().getElementsByClass(Constants.HTML.ELEMENT_CLASS_TD_TITLE).first()
        val text = termElement.text().trim()

        var startYear: Int? = null
        var endYear: Int? = null
        var termNum: Short? = null
        var nextTerm = false

        val strStuck = StringBuilder()
        for (c in text) {
            if (nextTerm) {
                if (c == FIRST_CHAR) {
                    termNum = 1
                } else if (c == SECOND_CHAR) {
                    termNum = 2
                }
                break
            } else if (c == ACADEMIC_YEAR_JOIN_SYMBOL) {
                startYear = strStuck.toString().toInt()
                strStuck.clear()
                continue
            } else if (c == STUDY_CHAR) {
                endYear = strStuck.toString().toInt()
                strStuck.clear()
                continue
            } else if (c == TIMES_CHAR) {
                nextTerm = true
                continue
            }
            strStuck.append(c)
        }
        strStuck.clear()
        return Term(startYear!!, endYear!!, termNum!!)
    }

    private fun getCourseSet(document: Document): HashSet<Course> {
        val contentElement = document.getElementById(Constants.HTML.ELEMENT_ID_CONTENT)
        val trElements = contentElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        if (trElements.isEmpty()) {
            return HashSet()
        }

        val courseSet = HashSet<Course>(trElements.size)

        var id: String? = null
        var name: String? = null
        var teacher: String? = null
        var courseClass: String? = null
        var teachClass: String? = null
        var credit: Float? = null
        var type: String? = null
        var timeSet: HashSet<CourseTime>? = null

        for (tr in 1 until trElements.size) {
            val tdElements = trElements[tr].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

            if (tdElements.size < 8) {
                throw IOException("Incomplete Course Data!")
            }

            for (td in 1 until tdElements.size) {
                when (td) {
                    1 -> id = tdElements[td].text()
                    2 -> name = tdElements[td].text()
                    3 -> teachClass = tdElements[td].text()
                    4 -> credit = tdElements[td].text().toFloat()
                    5 -> courseClass = tdElements[td].text()
                    6 -> type = tdElements[td].text()
                    7 -> teacher = tdElements[td].text()
                    8 -> timeSet = getCourseTimeSet(id!!, tdElements[td])
                }
            }

            courseSet.add(
                Course(
                    id!!,
                    name!!,
                    teacher!!,
                    courseClass!!,
                    teachClass!!,
                    credit!!,
                    type!!,
                    timeSet!!
                )
            )
        }

        return courseSet
    }

    internal fun getCourseTimeSet(courseId: String, element: Element, isNextTermCourse: Boolean = false): HashSet<CourseTime> {
        val text = element.text().trim()
        if (text.isBlank()) {
            return HashSet()
        }

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

        var weekStr: String
        var courseNumText: String
        var courseTimeSplit: List<String>

        for (str in courseLocationSplit) {
            weeksPeriodArray = ArrayList(2)
            coursesNumPeriodArray = ArrayList(1)

            courseTimeSplit = str.split(COURSE_TIME_STR)
            location = courseTimeSplit[0].trimEnd()

            if (isNextTermCourse) {
                weekStr = courseTimeSplit[1].substring(0, courseTimeSplit[1].indexOf(WEEK_NUM_CHAR) + 1)
                if (weekStr.isBlank()) {
                    continue
                }

                weekDay = courseTimeSplit[1].substring(weekStr.length + 1, weekStr.length + 1 + 1).toShort()

                rawCourseNumStr = courseTimeSplit[1].substring(weekStr.length + 1 + 1, courseTimeSplit[1].length)

                courseNumText = rawCourseNumStr.substring(1, rawCourseNumStr.indexOf(COURSE_NUM_CHAR))
            } else {
                val timeSplit = courseTimeSplit[1].split(COURSE_TIME_JOIN_SYMBOL)
                weekStr = timeSplit[0]
                if (weekStr.isBlank()) {
                    continue
                }

                weekDay = timeSplit[2].toShort()

                courseNumText = timeSplit[4].substring(0, timeSplit[4].indexOf(COURSE_NUM_CHAR))

                rawCourseNumStr = if (!courseNumText.startsWith(TIMES_CHAR)) {
                    TIMES_CHAR + timeSplit[4]
                } else {
                    timeSplit[4]
                }
            }

            weekNumAnalyse(weekStr, weeksPeriodArray).let { data ->
                weeksArr = data.first
                weekMode = data.second
            }

            rawWeeksStr = rawWeekStrAnalyse(weekStr)
            courseNumArr = courseNumAnalyse(courseNumText, coursesNumPeriodArray)

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

    private fun rawWeekStrAnalyse(weekStr: String) =
        if (!weekStr.startsWith(TIMES_CHAR)) {
            TIMES_CHAR + weekStr
        } else {
            weekStr
        }

    private fun weekNumAnalyse(weekStr: String, weeksPeriodArray: ArrayList<TimePeriod>): Pair<CharArray?, WeekMode> {
        var weeksArrTemp: CharArray? = null
        var weekMode = WeekMode.ALL_WEEKS

        if (weekStr.startsWith(TIMES_CHAR)) {
            val weekNumStr = weekStr.substring(1, weekStr.indexOf(WEEK_NUM_CHAR))
            if (MULTI_TIME_JOIN_SYMBOL in weekNumStr) {
                weekNumStr.split(MULTI_TIME_JOIN_SYMBOL).forEach { time ->
                    weeksArrTemp = if (FROM_TO_TIME_JOIN_SYMBOL in time) {
                        val temp = time.split(FROM_TO_TIME_JOIN_SYMBOL)
                        val periodTemp = TimePeriod(temp[0].toInt(), temp[1].toInt())
                        weeksPeriodArray.add(periodTemp)
                        periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, source = weeksArrTemp)
                    } else {
                        val periodTemp = TimePeriod(time.toInt())
                        weeksPeriodArray.add(periodTemp)
                        periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, source = weeksArrTemp)
                    }
                }
            } else {
                weeksArrTemp = if (FROM_TO_TIME_JOIN_SYMBOL in weekNumStr) {
                    val temp = weekNumStr.split(FROM_TO_TIME_JOIN_SYMBOL)
                    val periodTemp = TimePeriod(temp[0].toInt(), temp[1].toInt())
                    weeksPeriodArray.add(periodTemp)
                    periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, source = weeksArrTemp)
                } else {
                    val periodTemp = TimePeriod(weekNumStr.toInt())
                    weeksPeriodArray.add(periodTemp)
                    periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, source = weeksArrTemp)
                }
            }
        } else if (WEEK_TYPE_AND_CHAR in weekStr) {
            val weekNum = weekStr.substring(0, weekStr.indexOf(WEEK_TYPE_AND_CHAR)).split(FROM_TO_TIME_JOIN_SYMBOL)
            val periodTemp = TimePeriod(weekNum[0].toInt(), weekNum[1].toInt())
            if (WEEK_TYPE_SINGLE_CHAR in weekStr) {
                weekMode = WeekMode.ODD_WEEK_ONLY
                weeksArrTemp = periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, oddMode = true, source = weeksArrTemp)
            } else if (WEEK_TYPE_DOUBLE_CHAR in weekStr) {
                weekMode = WeekMode.EVEN_WEEK_ONLY
                weeksArrTemp = periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, evenMode = true, source = weeksArrTemp)
            }
            weeksPeriodArray.add(periodTemp)
        } else {
            val weekNum = weekStr.substring(0, weekStr.indexOf(WEEK_NUM_CHAR)).split(FROM_TO_TIME_JOIN_SYMBOL)
            val periodTemp = TimePeriod(weekNum[0].toInt(), weekNum[1].toInt())
            weeksPeriodArray.add(periodTemp)
            weeksArrTemp = periodTemp.convertToCharArray(Constants.Course.MAX_WEEK_NUM_SIZE, source = weeksArrTemp)
        }

        return weeksArrTemp to weekMode
    }

    private fun courseNumAnalyse(courseNumText: String, coursesNumPeriodArray: ArrayList<TimePeriod>): CharArray? {
        var courseNumArrTemp: CharArray? = null
        when {
            MULTI_TIME_JOIN_SYMBOL in courseNumText -> courseNumText.split(MULTI_TIME_JOIN_SYMBOL).forEach { time ->
                val periodTemp = TimePeriod(time.toInt())
                coursesNumPeriodArray.add(periodTemp)
                courseNumArrTemp = periodTemp.convertToCharArray(Constants.Course.MAX_COURSE_LENGTH, source = courseNumArrTemp)
            }
            FROM_TO_TIME_JOIN_SYMBOL in courseNumText -> {
                val splitTemp = courseNumText.split(FROM_TO_TIME_JOIN_SYMBOL)
                val periodTemp = TimePeriod(splitTemp[0].toInt(), splitTemp[1].toInt())
                coursesNumPeriodArray.add(periodTemp)
                courseNumArrTemp = periodTemp.convertToCharArray(Constants.Course.MAX_COURSE_LENGTH, source = courseNumArrTemp)
            }
            else -> {
                val periodTemp = TimePeriod(courseNumText.toInt())
                coursesNumPeriodArray.add(periodTemp)
                courseNumArrTemp = periodTemp.convertToCharArray(Constants.Course.MAX_COURSE_LENGTH, source = courseNumArrTemp)
            }
        }
        return courseNumArrTemp
    }
}