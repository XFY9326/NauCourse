package tool.xfy9326.naucourses.providers.contents.base

import okhttp3.HttpUrl
import okhttp3.Response
import org.jsoup.HttpStatusException
import tool.xfy9326.naucourses.providers.contents.beans.GeneralNews
import tool.xfy9326.naucourses.providers.contents.beans.GeneralNewsDetail
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseNewsContent<T> : BaseNoParamContent<Set<GeneralNews>>() {
    protected abstract fun convertToGeneralNews(newsData: Set<T>): Set<GeneralNews>

    protected abstract fun onParseRawData(content: String): Set<T>

    private fun requestDetailData(url: HttpUrl): RequestResult = try {
        onRequestDetailData(url).use {
            if (!it.isSuccessful) {
                throw HttpStatusException("Detail Request Failed!", it.code, it.request.url.toString())
            } else {
                RequestResult(true, contentData = it.body?.string()!!)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        when (e) {
            is SocketTimeoutException -> RequestResult(false, ContentErrorReason.TIMEOUT)
            is HttpStatusException -> RequestResult(false, ContentErrorReason.SERVER_ERROR)
            is IOException, is NullPointerException -> RequestResult(false, ContentErrorReason.OPERATION)
            else -> RequestResult(false, ContentErrorReason.UNKNOWN)
        }
    }

    protected abstract fun onRequestDetailData(url: HttpUrl): Response

    protected abstract fun onParseDetailData(content: String): GeneralNewsDetail

    private fun parseDetailData(contentData: String): ParseResult<GeneralNewsDetail> = try {
        ParseResult(true, onParseDetailData(contentData))
    } catch (e: Exception) {
        e.printStackTrace()
        ParseResult(false)
    }

    final override fun onParseData(content: String): Set<GeneralNews> = convertToGeneralNews(onParseRawData(content))

    @Synchronized
    fun getContentDetailData(url: HttpUrl): ContentResult<GeneralNewsDetail> {
        val requestResult = requestDetailData(url)
        return if (requestResult.isRequestSuccess) {
            val parseResult = parseDetailData(requestResult.contentData!!)
            if (parseResult.isParseSuccess) {
                ContentResult(true, contentData = parseResult.parseData)
            } else {
                ContentResult(false, ContentErrorReason.PRASE_FAILED)
            }
        } else {
            ContentResult(false, requestResult.requestContentErrorResult)
        }
    }
}