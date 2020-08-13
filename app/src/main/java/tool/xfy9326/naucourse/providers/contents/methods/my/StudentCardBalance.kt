package tool.xfy9326.naucourse.providers.contents.methods.my

import okhttp3.HttpUrl
import okhttp3.Response
import org.json.JSONObject
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.MyClient
import tool.xfy9326.naucourse.providers.beans.ykt.CardBalance
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent


object StudentCardBalance : BaseNoParamContent<CardBalance>() {
    override val networkClient = getLoginClient<MyClient>(LoginNetworkManager.ClientType.MY)

    private const val PATH_WEB = "_web"
    private const val PATH_PLUGS = "_plugs"
    private const val PATH_NOTES = "notes"
    private const val PATH_SELECT_ID = "selectNotesDefById.rst"
    private const val PARAM_ID_NAME = "id"
    private const val PARAM_ID = "1"

    private const val JSON_KEY_COUNT = "count"

    private val STUDENT_CARD_BALANCE_URL = HttpUrl.Builder().scheme(NetworkConst.HTTP).host(MyClient.MY_SERVER).addPathSegment(PATH_WEB)
        .addPathSegment(PATH_PLUGS).addPathSegment(PATH_NOTES).addPathSegment(PATH_SELECT_ID).addQueryParameter(PARAM_ID_NAME, PARAM_ID).build()

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(STUDENT_CARD_BALANCE_URL)

    override fun onParseData(content: String) = CardBalance(JSONObject(content).getString(JSON_KEY_COUNT).toFloat(), 0f)
}