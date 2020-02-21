package tool.xfy9326.naucourses.providers.contents.methods.my

import okhttp3.HttpUrl
import okhttp3.Response
import org.json.JSONObject
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.network.SSONetworkManager
import tool.xfy9326.naucourses.network.clients.MyClient
import tool.xfy9326.naucourses.providers.contents.base.BaseNoParamContent
import java.io.IOException


object StudentCardBalance : BaseNoParamContent<Float>() {
    private val myClient = getSSOClient<MyClient>(SSONetworkManager.ClientType.MY)

    private const val MY_WEB_PATH = "_web"
    private const val MY_PLUGS_PATH = "_plugs"
    private const val MY_NOTES_PATH = "notes"
    private const val MY_SELECT_NOTES_DEF_BY_ID_RST = "selectNotesDefById.rst"

    private const val MY_ID_PARAM = "id"
    private const val MY_ID_VALUE = "1"

    private const val STUDENT_CARD_JSON_PROPERTY_NAME = "count"

    private const val STUDENT_CARD_STR = "一卡通"

    private val STUDENT_CARD_BALANCE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(MyClient.MY_HOST)
        .addPathSegment(MY_WEB_PATH).addPathSegment(MY_PLUGS_PATH).addPathSegment(MY_NOTES_PATH)
        .addPathSegment(MY_SELECT_NOTES_DEF_BY_ID_RST).addQueryParameter(MY_ID_PARAM, MY_ID_VALUE).build()

    override fun onRequestData(): Response = myClient.newAutoLoginCall(STUDENT_CARD_BALANCE_URL)

    override fun onParseData(content: String): Float {
        if (STUDENT_CARD_STR in content) {
            val jsonObject = JSONObject(content)
            return jsonObject.getString(STUDENT_CARD_JSON_PROPERTY_NAME).toFloat()
        } else {
            throw IOException("Student Card Balance API Error!")
        }
    }
}