package tool.xfy9326.naucourse.providers.contents.methods.ykt

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.CardBalance
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.clients.YktClient
import tool.xfy9326.naucourse.providers.contents.base.BaseNoParamContent


object StudentCardBalance : BaseNoParamContent<CardBalance>() {
    override val networkClient = getLoginClient<YktClient>(LoginNetworkManager.ClientType.YKT)

    private const val SELF_SEARCH_PATH = "SelfSearch"
    private const val USER_PATH = "User"
    private const val HOME_ASPX = "Home.aspx"

    private const val PERSONAL_NEWS_CLASS = "person_news"
    private const val INFO_DIVIDE_SYMBOL = "："

    private val STUDENT_CARD_BALANCE_URL = HttpUrl.Builder().scheme(Constants.Network.HTTP).host(YktClient.YKT_HOST).addPathSegment(SELF_SEARCH_PATH)
        .addPathSegment(USER_PATH).addPathSegment(HOME_ASPX).build()

    override fun onRequestData(): Response = networkClient.newAutoLoginCall(STUDENT_CARD_BALANCE_URL)

    override fun onParseData(content: String): CardBalance {
        val body = Jsoup.parse(content).body()
        val personalNews = body.getElementsByClass(PERSONAL_NEWS_CLASS)[0]
        val li = personalNews.getElementsByTag(Constants.HTML.ELEMENT_TAG_LI)
        val main = li[1].getElementsByTag(Constants.HTML.ELEMENT_TAG_SPAN)[1].text().trim()
        val support = li[2].getElementsByTag(Constants.HTML.ELEMENT_TAG_SPAN)[1].text().trim()

        val mainBalance = main.substring(main.indexOf(INFO_DIVIDE_SYMBOL) + 1, main.length - 1).toFloat()
        val supportBalance = support.substring(support.indexOf(INFO_DIVIDE_SYMBOL) + 1, support.length - 1).toFloat()

        return CardBalance(mainBalance, supportBalance)
    }
}