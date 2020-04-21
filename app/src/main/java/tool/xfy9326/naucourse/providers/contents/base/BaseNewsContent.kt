package tool.xfy9326.naucourse.providers.contents.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.clients.base.ServerErrorException
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseNewsContent<T> : BaseNoParamContent<Set<GeneralNews>>() {
    protected open fun getDetailNetworkClient() = networkClient

    protected abstract fun convertToGeneralNews(newsData: Set<T>): Set<GeneralNews>

    protected abstract fun onParseRawData(content: String): Set<T>

    private fun requestDetailData(url: HttpUrl): RequestResult = try {
        onRequestDetailData(url).use {
            if (!it.isSuccessful) {
                throw ServerErrorException("Content Request Failed! Status: ${it.code} Url: ${it.request.url}")
            } else {
                RequestResult(true, contentData = it.body?.string()!!)
            }
        }
    } catch (e: Exception) {
        ExceptionUtils.printStackTrace<BaseNewsContent<T>>(e)
        when (e) {
            is SocketTimeoutException -> RequestResult(false, ContentErrorReason.TIMEOUT)
            is HttpStatusException, is ServerErrorException -> RequestResult(false, ContentErrorReason.SERVER_ERROR)
            is IOException, is NullPointerException -> RequestResult(false, ContentErrorReason.OPERATION)
            else -> RequestResult(false, ContentErrorReason.UNKNOWN)
        }
    }

    protected abstract fun onRequestDetailData(url: HttpUrl): Response

    protected open fun onBuildImageUrl(source: String): HttpUrl = source.toHttpUrl()

    fun getNewsImage(source: String): Pair<String, Bitmap>? {
        try {
            val imageUrl = if (source.startsWith(Constants.Network.HTTP)) {
                source.toHttpUrl()
            } else {
                onBuildImageUrl(source)
            }
            val response = onRequestDetailData(imageUrl)
            response.body?.byteStream()?.let {
                return Pair(imageUrl.toString(), BitmapFactory.decodeStream(it))
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<BaseNewsContent<T>>(e)
            return null
        }
        return null
    }

    protected abstract fun onParseDetailData(document: Document): GeneralNewsDetail

    private fun parseDetailData(contentData: String): ParseResult<GeneralNewsDetail> = try {
        val document = Jsoup.parse(contentData)
        imgTagFormat(document)
        ParseResult(true, onParseDetailData(document))
    } catch (e: Exception) {
        ExceptionUtils.printStackTrace<BaseNewsContent<T>>(e)
        ParseResult(false)
    }

    private fun imgTagFormat(document: Document) {
        val imgTag = document.body()?.getElementsByTag(Constants.HTML.ELEMENT_TAG_IMG)
        if (imgTag != null) {
            for (element in imgTag) {
                val src = element.attr("src")
                element.clearAttributes().attr("src", src)
                element.previousElementSibling()?.appendElement(Constants.HTML.ELEMENT_TAG_BR)
                element.appendElement(Constants.HTML.ELEMENT_TAG_BR)
            }
        }
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
                ContentResult(false, ContentErrorReason.PARSE_FAILED)
            }
        } else {
            ContentResult(false, requestResult.requestContentErrorResult)
        }
    }
}