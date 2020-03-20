package tool.xfy9326.naucourses.providers.contents.methods.jwc

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.LoginNetworkManager
import tool.xfy9326.naucourses.network.clients.JwcClient
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourses.providers.beans.jwc.StudentLearningProcess
import tool.xfy9326.naucourses.providers.beans.jwc.StudentPersonalInfo
import tool.xfy9326.naucourses.providers.contents.base.BaseNoParamContent
import java.io.IOException
import kotlin.math.min

object StudentIndex : BaseNoParamContent<StudentInfo>() {
    override val networkClient = getLoginClient<JwcClient>(LoginNetworkManager.ClientType.JWC)

    private const val JWC_STUDENT_INDEX_ASPX = "StudentIndex.aspx"
    private const val JWC_STU_PHOTO_ASHX = "StuPhotoView.ashx"
    private const val URL_PARAM_T = "t"
    private const val URL_PARAM_T_VALUE = "1"

    private const val ELEMENT_ID_MY_LEARNING_PROCESS = "MyLearningProcess"

    private const val ELEMENT_ID_BX = "BX"
    private const val ELEMENT_ID_ZX = "ZX"
    private const val ELEMENT_ID_GC = "GX"
    private const val ELEMENT_ID_SJ = "SJ"

    private const val CREDIT_AND_RANKING_INFO_STR = "学分排名信息"

    private const val SELECT_CREDIT_AND_RANKING_PATH =
        "${Constants.HTML.ELEMENT_TAG_DIV}[${Constants.HTML.ELEMENT_ATTR_TITLE}=$CREDIT_AND_RANKING_INFO_STR]"

    private val JWC_STUDENT_INDEX_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(JWC_STUDENT_INDEX_ASPX).build()

    val JWC_STU_PHOTO_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(JwcClient.JWC_HOST)
        .addPathSegment(JwcClient.JWC_STUDENTS_PATH).addPathSegment(JWC_STU_PHOTO_ASHX).addQueryParameter(URL_PARAM_T, URL_PARAM_T_VALUE).build()

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(JWC_STUDENT_INDEX_URL)

    override fun onParseData(content: String): StudentInfo {
        val document = Jsoup.parse(content)
        val bodyElement = document.body()
        val creditAndRankInfoElements = bodyElement.select(SELECT_CREDIT_AND_RANKING_PATH)

        val personalInfo = getPersonalInfo(bodyElement)
        val learningProcess = getLearningProcess(bodyElement)
        val creditInfo = getCreditInfo(creditAndRankInfoElements[0])
        val rankingInfo = getRankingInfo(creditAndRankInfoElements[1])

        return StudentInfo(personalInfo, learningProcess, creditInfo, rankingInfo)
    }

    private fun getPersonalInfo(bodyElement: Element): StudentPersonalInfo {
        val tableElements = bodyElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TABLE).first()
        val trElements = tableElements.getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        var stuId: Pair<String, String>? = null
        var name: Pair<String, String>? = null
        var grade: Pair<String, String>? = null
        var college: Pair<String, String>? = null
        var major: Pair<String, String>? = null
        var majorDirection: Pair<String, String>? = null
        var trainingDirection: Pair<String, String>? = null
        var currentClass: Pair<String, String>? = null

        for ((index, trElement) in trElements.withIndex()) {
            val tdElements = trElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)
            val pair = Pair(tdElements[0].text(), tdElements[1].text().trim())
            when (index) {
                0 -> stuId = pair
                1 -> name = pair
                2 -> grade = pair
                3 -> college = pair
                4 -> major = pair
                5 -> majorDirection = pair
                6 -> trainingDirection = pair
                7 -> currentClass = pair
            }
        }

        return StudentPersonalInfo(stuId!!, name!!, grade!!, college!!, major!!, majorDirection!!, trainingDirection!!, currentClass!!)
    }

    private fun getLearningProcess(bodyElement: Element): Array<StudentLearningProcess> {
        val trElements = bodyElement.getElementById(ELEMENT_ID_MY_LEARNING_PROCESS).getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        val learningProcessArr = arrayOfNulls<StudentLearningProcess>(trElements.size)

        var courseType: StudentLearningProcess.CourseType
        var title: String
        var progress: Int
        var subjects: LinkedHashMap<StudentLearningProcess.SubjectType, Float>

        for ((arrIndex, trElement) in trElements.withIndex()) {
            val tdElements = trElement.getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)
            title = tdElements[0].text()
            val divElements = tdElements[1].getElementsByTag(Constants.HTML.ELEMENT_TAG_DIV)
            progress = divElements[0].attr(Constants.HTML.ELEMENT_ATTR_VALUE).toInt()
            courseType = when (divElements[0].attr(Constants.HTML.ELEMENT_ATTR_ID)) {
                ELEMENT_ID_BX -> StudentLearningProcess.CourseType.COMPULSORY
                ELEMENT_ID_ZX -> StudentLearningProcess.CourseType.MAJOR_SELECTIVE
                ELEMENT_ID_GC -> StudentLearningProcess.CourseType.OPTIONAL
                ELEMENT_ID_SJ -> StudentLearningProcess.CourseType.PRACTICAL
                else -> throw IOException("Unknown Learning Process Course Type! Type Title: $title")
            }
            val spanElements = divElements[1].getElementsByTag(Constants.HTML.ELEMENT_TAG_SPAN)
            subjects = LinkedHashMap(spanElements.size)
            when (spanElements.size) {
                3 -> {
                    subjects[StudentLearningProcess.SubjectType.TARGET] = spanElements[0].text().toFloat()
                    subjects[StudentLearningProcess.SubjectType.REVISED] = spanElements[1].text().toFloat()
                    subjects[StudentLearningProcess.SubjectType.BALANCE] = spanElements[2].text().toFloat()
                }
                4 -> {
                    subjects[StudentLearningProcess.SubjectType.TARGET] = spanElements[0].text().toFloat()
                    subjects[StudentLearningProcess.SubjectType.REVISED] = spanElements[1].text().toFloat()
                    subjects[StudentLearningProcess.SubjectType.BONUS] = spanElements[2].text().toFloat()
                    subjects[StudentLearningProcess.SubjectType.BALANCE] = spanElements[3].text().toFloat()
                }
                else -> {
                    throw IOException("Unknown Learning Process Subjects! Title: $title  Subject Size: ${spanElements.size}")
                }
            }

            learningProcessArr[arrIndex] = StudentLearningProcess(courseType, title, progress, subjects)
        }

        return learningProcessArr.requireNoNulls()
    }

    private fun getCreditInfo(creditAndRankInfoElements0: Element): LinkedHashMap<String, Float> {
        val trElements = creditAndRankInfoElements0.getElementsByTag(Constants.HTML.ELEMENT_TAG_TR)

        val thElements = trElements[0].getElementsByTag(Constants.HTML.ELEMENT_TAG_TH)
        val tdElements = trElements[1].getElementsByTag(Constants.HTML.ELEMENT_TAG_TD)

        val size = min(thElements.size, tdElements.size)
        val creditInfoArr = LinkedHashMap<String, Float>(size)

        for (i in 0 until size) {
            creditInfoArr[thElements[i].text()] = tdElements[i].text().toFloat()
        }

        return creditInfoArr
    }

    private fun getRankingInfo(creditAndRankInfoElements1: Element): LinkedHashMap<String, String> {
        val spanElements = creditAndRankInfoElements1.getElementsByTag(Constants.HTML.ELEMENT_TAG_SPAN)

        val size = spanElements.size / 2
        val rankInfoArr = LinkedHashMap<String, String>(size)

        for (i in 1..size step 2) {
            rankInfoArr[spanElements[i / 2].ownText()] = spanElements[1 / 2 + 1].ownText()
        }

        return rankInfoArr
    }
}