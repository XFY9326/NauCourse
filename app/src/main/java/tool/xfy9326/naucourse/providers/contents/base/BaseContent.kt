package tool.xfy9326.naucourse.providers.contents.base

import okhttp3.Response
import org.jsoup.HttpStatusException
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.network.SimpleNetworkManager
import tool.xfy9326.naucourse.network.clients.base.BaseLoginClient
import tool.xfy9326.naucourse.network.clients.base.BaseNetworkClient
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseContent<T> {
    protected abstract val networkClient: BaseNetworkClient

    @Suppress("UNCHECKED_CAST")
    protected fun <E : BaseLoginClient> getLoginClient(type: LoginNetworkManager.ClientType): E =
        LoginNetworkManager.getClient(type) as E

    protected fun getSimpleClient() = SimpleNetworkManager.getClient()

    private fun requestData(): RequestResult = try {
        onRequestData().use {
            if (!it.isSuccessful) {
                throw HttpStatusException("Content Request Failed!", it.code, it.request.url.toString())
            } else {
                RequestResult(true, contentData = it.body?.string()!!)
            }
        }
    } catch (e: Exception) {
        ExceptionUtils.printStackTrace<BaseContent<T>>(e)
        when (e) {
            is SocketTimeoutException -> RequestResult(false, ContentErrorReason.TIMEOUT)
            is UnknownHostException, is HttpStatusException -> RequestResult(false, ContentErrorReason.SERVER_ERROR)
            is IOException, is NullPointerException -> RequestResult(false, ContentErrorReason.OPERATION)
            is ConnectException -> RequestResult(false, ContentErrorReason.CONNECTION_ERROR)
            else -> RequestResult(false, ContentErrorReason.UNKNOWN)
        }
    }

    protected abstract fun onRequestData(): Response

    private fun parseData(contentData: String): ParseResult<T> = try {
        ParseResult(true, onParseData(contentData))
    } catch (e: Exception) {
        ExceptionUtils.printStackTrace<BaseContent<T>>(e)
        ParseResult(false)
    }

    protected abstract fun onParseData(content: String): T

    protected fun requestAndParse(): ContentResult<T> {
        val requestResult = requestData()
        return if (requestResult.isRequestSuccess) {
            val parseResult = parseData(requestResult.contentData!!)
            if (parseResult.isParseSuccess) {
                ContentResult(true, contentData = parseResult.parseData)
            } else {
                ContentResult(false, ContentErrorReason.PARSE_FAILED)
            }
        } else {
            ContentResult(false, requestResult.requestContentErrorResult)
        }
    }
}