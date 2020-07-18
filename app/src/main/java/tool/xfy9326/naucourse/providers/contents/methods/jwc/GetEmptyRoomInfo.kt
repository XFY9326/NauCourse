package tool.xfy9326.naucourse.providers.contents.methods.jwc

import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.network.clients.JwcClient
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomSearchParam
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomSearchResult
import tool.xfy9326.naucourse.providers.contents.base.BaseParamContent
import java.text.SimpleDateFormat
import java.util.*

object GetEmptyRoomInfo : BaseParamContent<Array<EmptyRoomSearchResult>, EmptyRoomSearchParam>() {
    private var searchParamParam: EmptyRoomSearchParam? = null

    private const val POST_PARAM_CAMPUS_NAME = "campusname"
    private const val POST_PARAM_SEARCH_DATE = "searchDate"
    private const val POST_PARAM_BJC = "BJC"
    private const val POST_PARAM_EJC = "EJC"
    private const val POST_PARAM_TERM = "term"
    private const val POST_PARAM_START_DATE = "startDate"
    private const val POST_PARAM_END_DATE = "endDate"

    private const val PAGE_URL = "GetEmptyRoomInfo.ashx"
    private val REQUEST_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(JwcClient.JWC_HOST).addPathSegment(PAGE_URL).build()
    private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)

    override val networkClient = getSimpleClient()

    override fun onParamSet(param: EmptyRoomSearchParam) {
        searchParamParam = param
    }

    override fun onRequestData(): Response {
        val request = Request.Builder().url(REQUEST_URL).apply {
            searchParamParam?.let {
                post(FormBody.Builder().apply {
                    add(POST_PARAM_CAMPUS_NAME, it.campusName)
                    add(POST_PARAM_SEARCH_DATE, DATE_FORMAT_YMD.format(it.searchDate))
                    add(POST_PARAM_BJC, it.BJC.toString())
                    add(POST_PARAM_EJC, it.EJC.toString())
                    add(POST_PARAM_TERM, it.term)
                    add(POST_PARAM_START_DATE, DATE_FORMAT_YMD.format(it.startDate))
                    add(POST_PARAM_END_DATE, DATE_FORMAT_YMD.format(it.endDate))
                }.build())
                searchParamParam = null
            }
        }.build()
        return networkClient.newClientCall(request)
    }

    override fun onParseData(content: String): Array<EmptyRoomSearchResult> {
        return Gson().fromJson(content, Array<EmptyRoomSearchResult>::class.java)
    }
}